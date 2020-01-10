/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.concourse.artifactoryresource.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryRepository;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.DeployOption;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableFileArtifact;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest.ArtifactSet;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest.Params;
import io.spring.concourse.artifactoryresource.command.payload.OutResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.io.DirectoryScanner;
import io.spring.concourse.artifactoryresource.io.PathFilter;
import io.spring.concourse.artifactoryresource.maven.MavenCoordinates;
import io.spring.concourse.artifactoryresource.maven.MavenVersionType;
import io.spring.concourse.artifactoryresource.system.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Delegate used to handle operations triggered from the {@link OutCommand}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class OutHandler {

	private static final Set<String> CHECKSUM_FILE_EXTENSIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(".md5", ".sha1", ".sha256", ".sha512")));

	private static final Set<String> METADATA_FILES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("maven-metadata.xml", "maven-metadata-local.xml")));

	private static final DeployOption[] NO_DEPLOY_OPTIONS = {};

	private static final DeployOption[] DISABLE_CHECKSUM_UPLOADS = { DeployOption.DISABLE_CHECKSUM_UPLOADS };

	private static final Logger logger = LoggerFactory.getLogger(OutHandler.class);

	private static final ConsoleLogger console = new ConsoleLogger();

	private final Artifactory artifactory;

	private final BuildNumberGenerator buildNumberGenerator;

	private final ModuleLayouts moduleLayouts;

	private final DirectoryScanner directoryScanner;

	public OutHandler(Artifactory artifactory, BuildNumberGenerator buildNumberGenerator, ModuleLayouts moduleLayouts,
			DirectoryScanner directoryScanner) {
		this.artifactory = artifactory;
		this.buildNumberGenerator = buildNumberGenerator;
		this.moduleLayouts = moduleLayouts;
		this.directoryScanner = directoryScanner;
	}

	public OutResponse handle(OutRequest request, Directory directory) {
		Source source = request.getSource();
		Params params = request.getParams();
		DebugLogging.setEnabled(params.isDebug());
		Assert.state(!directory.isEmpty(), "No artifacts found in empty directory");
		String buildNumber = getOrGenerateBuildNumber(params);
		ArtifactoryServer artifactoryServer = getArtifactoryServer(source);
		List<DeployableArtifact> artifacts = getDeployableArtifacts(buildNumber, source, params, directory);
		Assert.state(artifacts.size() > 0, "No artifacts found to deploy");
		console.log("Deploying {} artifacts to {} as build {} using {} thread(s)", artifacts.size(), source.getUri(),
				buildNumber, params.getThreads());
		deployArtifacts(artifactoryServer, params, artifacts);
		addBuildRun(artifactoryServer, source, params, buildNumber, artifacts);
		logger.debug("Done");
		return new OutResponse(new Version(buildNumber));
	}

	private ArtifactoryServer getArtifactoryServer(Source source) {
		logger.debug("Using artifactory server " + source.getUri());
		return this.artifactory.server(source.getUri(), source.getUsername(), source.getPassword());
	}

	private String getOrGenerateBuildNumber(Params params) {
		if (StringUtils.hasLength(params.getBuildNumber())) {
			return params.getBuildNumber();
		}
		String buildNumber = this.buildNumberGenerator.generateBuildNumber();
		logger.debug("Generated build number {}", buildNumber);
		return buildNumber;
	}

	private List<DeployableArtifact> getDeployableArtifacts(String buildNumber, Source source, Params params,
			Directory directory) {
		Directory root = directory.getSubDirectory(params.getFolder());
		logger.debug("Getting deployable artifacts from {}", root);
		Stream<File> files = this.directoryScanner.scan(root, params.getInclude(), params.getExclude()).stream();
		files = files.filter(getChecksumFilter());
		files = files.filter(getMetadataFilter(params));
		return files.map((file) -> {
			String path = DeployableFileArtifact.calculatePath(root.getFile(), file);
			logger.debug("Including file {} with path {}", file, path);
			Map<String, String> properties = getDeployableArtifactProperties(path, buildNumber, source, params);
			if (params.isStripSnapshotTimestamps()) {
				path = stripSnapshotTimestamp(path);
			}
			return new DeployableFileArtifact(path, file, properties, null);
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	private Map<String, String> getDeployableArtifactProperties(String path, String buildNumber, Source source,
			Params params) {
		Map<String, String> properties = new LinkedHashMap<>();
		addArtifactSetProperties(path, params, properties);
		addBuildProperties(buildNumber, source, properties);
		return properties;
	}

	private void addArtifactSetProperties(String path, Params params, Map<String, String> properties) {
		for (ArtifactSet artifactSet : params.getArtifactSet()) {
			if (getFilter(artifactSet).isMatch(path)) {
				logger.debug("Artifact set matched, adding properties {}", artifactSet.getProperties());
				properties.putAll(artifactSet.getProperties());
			}
		}
	}

	private PathFilter getFilter(ArtifactSet artifactSet) {
		logger.debug("Creating artifact set filter including {} and excluding {}", artifactSet.getInclude(),
				artifactSet.getExclude());
		return new PathFilter(artifactSet.getInclude(), artifactSet.getExclude());
	}

	private void addBuildProperties(String buildNumber, Source source, Map<String, String> properties) {
		properties.put("build.name", source.getBuildName());
		properties.put("build.number", buildNumber);
	}

	private String stripSnapshotTimestamp(String path) {
		MavenCoordinates coordinates = MavenCoordinates.fromPath(path);
		if (coordinates.getVersionType() != MavenVersionType.TIMESTAMP_SNAPSHOT) {
			return path;
		}
		String stripped = path.replace(coordinates.getSnapshotVersion(), coordinates.getVersion());
		logger.debug("Stripped timestamp version {} to {}", path, stripped);
		return stripped;
	}

	private void deployArtifacts(ArtifactoryServer artifactoryServer, Params params,
			List<DeployableArtifact> deployableArtifacts) {
		logger.debug("Deploying artifacts to {}", params.getRepo());
		ArtifactoryRepository artifactoryRepository = artifactoryServer.repository(params.getRepo());
		DeployOption[] options = params.isDisableChecksumUploads() ? DISABLE_CHECKSUM_UPLOADS : NO_DEPLOY_OPTIONS;
		ExecutorService executor = Executors.newFixedThreadPool(params.getThreads());
		Function<DeployableArtifact, CompletableFuture<?>> deployer = (deployableArtifact) -> getArtifactDeployer(
				artifactoryRepository, options, deployableArtifact);
		try {
			getDeployableBatches(deployableArtifacts)
					.forEach((batchName, batch) -> deployBatch(batchName, batch, deployer));
		}
		finally {
			executor.shutdown();
		}
	}

	private MultiValueMap<String, DeployableArtifact> getDeployableBatches(
			List<DeployableArtifact> deployableArtifacts) {
		MultiValueMap<String, DeployableArtifact> batches = new LinkedMultiValueMap<>();
		for (DeployableArtifact deployableArtifact : deployableArtifacts) {
			String extension = StringUtils.getFilenameExtension(deployableArtifact.getPath());
			String batchName = (extension != null) ? "with the extension " + extension : "without an extension";
			batches.add(batchName, deployableArtifact);
		}
		return batches;
	}

	private void deployBatch(String batchName, List<DeployableArtifact> batch,
			Function<DeployableArtifact, CompletableFuture<?>> artifactDeployer) {
		logger.debug("Deploying artifacts {}", batchName);
		deployBatch(batch.stream().map(artifactDeployer).toArray(CompletableFuture[]::new));
	}

	private void deployBatch(CompletableFuture<?>[] batch) {
		try {
			CompletableFuture.allOf(batch).get();
		}
		catch (ExecutionException ex) {
			throw new RuntimeException(ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private CompletableFuture<?> getArtifactDeployer(ArtifactoryRepository artifactoryRepository,
			DeployOption[] options, DeployableArtifact deployableArtifact) {
		return CompletableFuture.runAsync(() -> deployArtifact(artifactoryRepository, deployableArtifact, options));
	}

	private void deployArtifact(ArtifactoryRepository artifactoryRepository, DeployableArtifact deployableArtifact,
			DeployOption[] options) {
		console.log("Deploying {} {} ({}/{})", deployableArtifact.getPath(), deployableArtifact.getProperties(),
				deployableArtifact.getChecksums().getSha1(), deployableArtifact.getChecksums().getMd5());
		artifactoryRepository.deploy(deployableArtifact, options);
	}

	private Predicate<File> getMetadataFilter(Params params) {
		if (params.isStripSnapshotTimestamps()) {
			return (file) -> !METADATA_FILES.contains(file.getName().toLowerCase());
		}
		return (file) -> true;
	}

	private Predicate<File> getChecksumFilter() {
		return (file) -> {
			String name = file.getName().toLowerCase();
			for (String extension : CHECKSUM_FILE_EXTENSIONS) {
				if (name.endsWith(extension)) {
					return false;
				}
			}
			return true;
		};
	}

	private void addBuildRun(ArtifactoryServer artifactoryServer, Source source, Params params, String buildNumber,
			List<DeployableArtifact> artifacts) {
		logger.debug("Adding build run {}", buildNumber);
		List<BuildModule> modules = this.moduleLayouts.getBuildModulesGenerator(params.getModuleLayout())
				.getBuildModules(artifacts);
		artifactoryServer.buildRuns(source.getBuildName()).add(buildNumber, params.getBuildUri(), modules);
	}

}
