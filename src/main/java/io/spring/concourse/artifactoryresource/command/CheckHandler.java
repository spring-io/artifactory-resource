/*
 * Copyright 2017-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.command.payload.CheckRequest;
import io.spring.concourse.artifactoryresource.command.payload.CheckResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Delegate used to handle operations triggered from the {@link CheckCommand}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class CheckHandler {

	private final Artifactory artifactory;

	public CheckHandler(Artifactory artifactory) {
		this.artifactory = artifactory;
	}

	public CheckResponse handle(CheckRequest request) {
		Source source = request.getSource();
		List<BuildRun> runs = getArtifactoryServer(source)
				.buildRuns(source.getBuildName()).getAll();
		if (request.getVersion() == null
				|| StringUtils.isEmpty(request.getVersion().getBuildNumber())) {
			return getLatest(runs);
		}
		return getAfter(runs, request.getVersion());
	}

	private ArtifactoryServer getArtifactoryServer(Source source) {
		return this.artifactory.server(source.getUri(), source.getUsername(),
				source.getPassword());
	}

	private CheckResponse getAfter(List<BuildRun> runs, Version version) {
		BuildRun versionRun = findBuildRun(runs, version);
		if (versionRun == null) {
			return getLatest(runs);
		}
		ArrayList<BuildRun> runsSince = runs.stream()
				.filter((run) -> run.compareTo(versionRun) >= 0)
				.collect(Collectors.toCollection(ArrayList::new));
		Collections.sort(runsSince);
		return new CheckResponse(runsSince.stream().map(this::asVersion)
				.collect(Collectors.toCollection(ArrayList::new)));
	}

	private BuildRun findBuildRun(List<BuildRun> runs, Version version) {
		return runs.stream()
				.filter((run) -> run.getBuildNumber().equals(version.getBuildNumber()))
				.findFirst().orElse(null);
	}

	private CheckResponse getLatest(List<BuildRun> runs) {
		return new CheckResponse(runs.stream().max(BuildRun::compareTo)
				.map(this::asVersion).map(Collections::singletonList)
				.orElseGet(Collections::emptyList));
	}

	private Version asVersion(BuildRun run) {
		return new Version(run.getBuildNumber());
	}

}
