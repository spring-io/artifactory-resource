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

import java.util.Collections;
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

	private final Artifactory artifactory;

	public CheckHandler(Artifactory artifactory) {
		this.artifactory = artifactory;
	}

	public CheckResponse handle(CheckRequest request) {
		Source source = request.getSource();
		Version version = request.getVersion();
		return new CheckResponse((version != null) ? getNewVersions(source, version) : getCurrentVersion(source));
	}

	private List<Version> getCurrentVersion(Source source) {
		String buildNumberPrefix = source.getBuildNumberPrefix();
		List<BuildRun> all = buildRuns(source).getAll(buildNumberPrefix);
		return getLatest(all).stream().map(this::asVersion).toList();
	}

	private List<Version> getNewVersions(Source source, Version version) {
		return getRunsStartedOnOrAfter(source, version).stream().sorted().map(this::asVersion).toList();
	}

	private List<BuildRun> getRunsStartedOnOrAfter(Source source, Version version) {
		ArtifactoryBuildRuns buildRuns = buildRuns(source);
		String buildNumberPrefix = source.getBuildNumberPrefix();
		if (version.getStarted() != null) {
			List<BuildRun> startedOnOrAfter = buildRuns.getStartedOnOrAfter(buildNumberPrefix, version.getStarted());
			return (!startedOnOrAfter.isEmpty()) ? startedOnOrAfter : getLatest(buildRuns.getAll(buildNumberPrefix));
		}
		List<BuildRun> all = buildRuns.getAll(buildNumberPrefix);
		BuildRun versionRun = findFirstOrNull(all, (run) -> isVersionMatch(run, version));
		Predicate<BuildRun> greaterThanOrEqualToVersionRun = (run) -> run.compareTo(versionRun) >= 0;
		return (versionRun != null) ? all.stream().filter(greaterThanOrEqualToVersionRun).toList() : getLatest(all);
	}

	private boolean isVersionMatch(BuildRun run, Version version) {
		boolean versionMatch = run.getBuildNumber().equals(version.getBuildNumber());
		boolean timestampMatch = version.getStarted() == null || run.getStarted().equals(version.getStarted());
		return versionMatch && timestampMatch;
	}

	private ArtifactoryBuildRuns buildRuns(Source source) {
		return artifactoryServer(source).buildRuns(source.getBuildName(), source.getCheckLimit());
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
