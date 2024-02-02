/*
 * Copyright 2017-2024 the original author or authors.
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryBuildRuns;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.command.payload.CheckRequest;
import io.spring.concourse.artifactoryresource.command.payload.CheckResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

/**
 * Delegate used to handle operations triggered from the {@link CheckCommand}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Gabriel Petrovay
 */
@Component
public class CheckHandler {

	private static final Logger logger = LoggerFactory.getLogger(CheckHandler.class);

	private final Artifactory artifactory;

	public CheckHandler(Artifactory artifactory) {
		this.artifactory = artifactory;
	}

	public CheckResponse handle(CheckRequest request) {
		Source source = request.getSource();
		Version version = request.getVersion();
		logger.debug("Handling check for source '{}' version '{}'", source, version);
		return new CheckResponse((version != null) ? getNewVersions(source, version) : getCurrentVersion(source));
	}

	private List<Version> getCurrentVersion(Source source) {
		logger.debug("Getting current version");
		String buildNumberPrefix = source.getBuildNumberPrefix();
		List<BuildRun> all = buildRuns(source).getAll(buildNumberPrefix);
		List<Version> latest = getLatest(all).stream().map(this::asVersion).toList();
		logger.debug("Found latest version {}", latest);
		return latest;
	}

	private List<Version> getNewVersions(Source source, Version version) {
		logger.debug("Getting new versions");
		List<Version> newVersions = getRunsStartedOnOrAfter(source, version).stream()
			.sorted(Comparator.reverseOrder())
			.map(this::asVersion)
			.toList();
		logger.debug("Found new versions {}", newVersions);
		return newVersions;
	}

	private List<BuildRun> getRunsStartedOnOrAfter(Source source, Version version) {
		ArtifactoryBuildRuns buildRuns = buildRuns(source);
		String buildNumberPrefix = source.getBuildNumberPrefix();
		if (version.getStarted() != null) {
			logger.debug("Getting version started on or after {}", version.getStarted());
			List<BuildRun> startedOnOrAfter = buildRuns.getStartedOnOrAfter(buildNumberPrefix, version.getStarted());
			return (!startedOnOrAfter.isEmpty()) ? startedOnOrAfter : getLatest(buildRuns.getAll(buildNumberPrefix));
		}
		logger.debug("Getting all versions in order to find version run");
		List<BuildRun> all = buildRuns.getAll(buildNumberPrefix);
		BuildRun versionRun = findFirstOrNull(all, (run) -> isVersionMatch(run, version));
		logger.debug("Found version run {}", versionRun);
		Predicate<BuildRun> greaterThanOrEqualToVersionRun = (run) -> run.compareTo(versionRun) >= 0;
		return (versionRun != null) ? all.stream().filter(greaterThanOrEqualToVersionRun).toList() : getLatest(all);
	}

	private boolean isVersionMatch(BuildRun run, Version version) {
		boolean versionMatch = run.getBuildNumber().equals(version.getBuildNumber());
		boolean timestampMatch = version.getStarted() == null || run.getStarted().equals(version.getStarted());
		return versionMatch && timestampMatch;
	}

	private ArtifactoryBuildRuns buildRuns(Source source) {
		ArtifactoryServer artifactoryServer = artifactoryServer(source);
		System.out.println(System.identityHashCode(artifactoryServer));
		return artifactoryServer.buildRuns(source.getBuildName(), source.getProject(), source.getCheckLimit());
	}

	private ArtifactoryServer artifactoryServer(Source source) {
		return this.artifactory.server(source.getUri(), source.getUsername(), source.getPassword(), source.getProxy());
	}

	private List<BuildRun> getLatest(List<BuildRun> all) {
		return all.stream().max(BuildRun::compareTo).map(Collections::singletonList).orElseGet(Collections::emptyList);
	}

	private BuildRun findFirstOrNull(List<BuildRun> all, Predicate<? super BuildRun> predicate) {
		return all.stream().filter(predicate).findFirst().orElse(null);
	}

	private Version asVersion(BuildRun run) {
		return new Version(run.getBuildNumber(), run.getStarted());
	}

}
