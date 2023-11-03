/*
 * Copyright 2017-2023 the original author or authors.
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
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryRepository;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.BuildNumber;
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
import io.spring.concourse.artifactoryresource.io.FileSet;
import io.spring.concourse.artifactoryresource.io.FileSet.Category;
import io.spring.concourse.artifactoryresource.io.PathFilter;
import io.spring.concourse.artifactoryresource.maven.MavenCoordinates;
import io.spring.concourse.artifactoryresource.maven.MavenVersionType;
import io.spring.concourse.artifactoryresource.openpgp.ArmoredAsciiSigner;
import io.spring.concourse.artifactoryresource.system.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
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
 * @author Gabriel Petrovay
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
		BuildNumber buildNumber = BuildNumber.of(source.getBuildNumberPrefix(), getOrGenerateBuildNumber(params));
		Instant started = Instant.now();
		ArtifactoryServer artifactoryServer = getArtifactoryServer(source);
		MultiValueMap<Category, DeployableArtifact> batchedArtifacts = getBatchedArtifacts(buildNumber, started, source,
				params, directory);
		if (StringUtils.hasText(params.getSigningKey())) {
			Map<String, String> properties = new LinkedHashMap<>();
			addBuildProperties(buildNumber, started, source, properties);
			batchedArtifacts = signArtifacts(batchedArtifacts, params.getSigningKey(), params.getSigningPassphrase(),
					Collections.unmodifiableMap(properties));
		}
		int size = batchedArtifacts.values().stream().mapToInt(List::size).sum();
		Assert.state(size > 0, "No artifacts found to deploy");
		console.log("Deploying {} artifacts to {} as build {} using {} thread(s)", size, source.getUri(), buildNumber,
				params.getThreads());
		deployArtifacts(artifactoryServer, params, batchedArtifacts);
		addBuildRun(artifactoryServer, source, params, buildNumber, started, batchedArtifacts);
		logger.debug("Done");
		return new OutResponse(new Version(buildNumber.toString(), started));
	}

	private ArtifactoryServer getArtifactoryServer(Source source) {
		logger.debug("Using artifactory server " + source.getUri());
		if (source.getProxy() != null) {
			logger.debug("Artifactory server configured to use proxy: {}", source.getProxy());
		}
		return this.artifactory.server(source.getUri(), source.getUsername(), source.getPassword(), source.getProxy());
	}

	private String getOrGenerateBuildNumber(Params params) {
		if (StringUtils.hasLength(params.getBuildNumber())) {
			return params.getBuildNumber();
		}
		String buildNumber = this.buildNumberGenerator.generateBuildNumber();
		logger.debug("Generated build number {}", buildNumber);
		return buildNumber;
	}

	private MultiValueMap<Category, DeployableArtifact> getBatchedArtifacts(BuildNumber buildNumber, Instant started,
			Source source, Params params, Directory directory) {
		Directory root = directory.getSubDirectory(params.getFolder());
		logger.debug("Getting deployable artifacts from {}", root);
		FileSet fileSet = this.directoryScanner.scan(root, params.getInclude(), params.getExclude())
				.filter(getChecksumFilter()).filter(getMetadataFilter(params));
		MultiValueMap<Category, DeployableArtifact> batchedArtifacts = new LinkedMultiValueMap<>();
		Set<String> paths = new HashSet<>();
		fileSet.batchedByCategory().forEach((category, files) -> {
			files.forEach((file) -> {
				String path = DeployableFileArtifact.calculatePath(root.getFile(), file);
				logger.debug("Including file {} with path {}", file, path);
				Map<String, String> properties = getDeployableArtifactProperties(path, buildNumber, started, source,
						params);
				if (params.isStripSnapshotTimestamps()) {
					path = stripSnapshotTimestamp(path);
				}
				if (paths.add(path)) {
					batchedArtifacts.add(category, new DeployableFileArtifact(path, file, properties, null));
				}
			});
		});
		return batchedArtifacts;
	}

	private Map<String, String> getDeployableArtifactProperties(String path, BuildNumber buildNumber, Instant started,
			Source source, Params params) {
		Map<String, String> properties = new LinkedHashMap<>();
		addArtifactSetProperties(path, params, properties);
		addBuildProperties(buildNumber, started, source, properties);
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

	private void addBuildProperties(BuildNumber buildNumber, Instant started, Source source,
			Map<String, String> properties) {
		properties.put("build.name", source.getBuildName());
		properties.put("build.number", buildNumber.toString());
		properties.put("build.timestamp", Long.toString(started.toEpochMilli()));
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

	private MultiValueMap<Category, DeployableArtifact> signArtifacts(
			MultiValueMap<Category, DeployableArtifact> batchedArtifacts, String signingKey, String signingPassphrase,
			Map<String, String> properties) {
		try {
			console.log("Signing artifacts");
			ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(signingKey, signingPassphrase);
			return new DeployableArtifactsSigner(signer, properties).sign(batchedArtifacts);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to sign artifacts", ex);
		}
	}

	private void deployArtifacts(ArtifactoryServer artifactoryServer, Params params,
			MultiValueMap<Category, DeployableArtifact> batchedArtifacts) {
		logger.debug("Deploying artifacts to {}", params.getRepo());
		ArtifactoryRepository artifactoryRepository = artifactoryServer.repository(params.getRepo());
		DeployOption[] options = params.isDisableChecksumUploads() ? DISABLE_CHECKSUM_UPLOADS : NO_DEPLOY_OPTIONS;
		ExecutorService executor = Executors.newFixedThreadPool(params.getThreads());
		Function<DeployableArtifact, CompletableFuture<?>> deployer = (deployableArtifact) -> getArtifactDeployer(
				artifactoryRepository, options, deployableArtifact);
		try {
			batchedArtifacts.forEach((category, artifacts) -> deploy(category, artifacts, deployer));
		}
		finally {
			executor.shutdown();
		}
	}

	private void deploy(Category category, List<DeployableArtifact> artifacts,
			Function<DeployableArtifact, CompletableFuture<?>> deployer) {
		logger.debug("Deploying {} artifacts", category);
		deploy(artifacts.stream().map(deployer).toArray(CompletableFuture[]::new));
	}

	private void deploy(CompletableFuture<?>[] batch) {
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

	private void addBuildRun(ArtifactoryServer artifactoryServer, Source source, Params params, BuildNumber buildNumber,
			Instant started, MultiValueMap<Category, DeployableArtifact> batchedArtifacts) {
		List<DeployableArtifact> artifacts = batchedArtifacts.values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
		logger.debug("Adding build run {}", buildNumber);
		List<BuildModule> modules = this.moduleLayouts.getBuildModulesGenerator(params.getModuleLayout())
				.getBuildModules(artifacts);
		Map<String, String> properties = loadProperties(params.getBuildProperties());
		artifactoryServer.buildRuns(source.getBuildName(), source.getProject()).add(buildNumber, started,
				params.getBuildUri(), properties, modules);
	}

	private Map<String, String> loadProperties(String propertiesFile) {
		if (StringUtils.hasText(propertiesFile)) {
			try {
				FileSystemResource resource = new FileSystemResource(propertiesFile);
				return loadProperties(new EncodedResource(resource, StandardCharsets.UTF_8));
			}
			catch (IOException ex) {
				throw new IllegalStateException("Unable to read build properties from file '" + propertiesFile + "'");
			}
		}
		return null;
	}

	private Map<String, String> loadProperties(EncodedResource encodedResource) throws IOException {
		Map<String, String> properties = new LinkedHashMap<>();
		try (Reader reader = encodedResource.getReader()) {
			new Properties() {

				@Override
				public synchronized Object put(Object key, Object value) {
					return properties.put((String) key, (String) value);
				}

			}.load(reader);
		}
		return properties;
	}

}
