/*
 * Copyright 2017-2018 the original author or authors.
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

import io.spring.concourse.artifactoryresource.artifactory.BuildModulesGenerator;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.Checksums;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;

/**
 * Mock {@link BuildModulesGenerator} implementation.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class MockBuildModulesGenerator implements BuildModulesGenerator {

	@Override
	public List<BuildModule> getBuildModules(
			List<DeployableArtifact> deployableArtifacts) {
		return deployableArtifacts.stream().map(this::getBuildModule)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private BuildModule getBuildModule(DeployableArtifact artifact) {
		Checksums checksums = artifact.getChecksums();
		BuildArtifact buildArtifact = new BuildArtifact("file", checksums.getSha1(),
				checksums.getMd5(), artifact.getPath());
		return new BuildModule(artifact.getPath(),
				Collections.singletonList(buildArtifact));
	}

}
