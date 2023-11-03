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

package io.spring.concourse.artifactoryresource.artifactory;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;

/**
 * Interface providing access to a specific artifactory server.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public interface ArtifactoryServer {

	/**
	 * Access a specific repository from the server.
	 * @param repositoryName the name of the repository
	 * @return the artifactory repository
	 */
	ArtifactoryRepository repository(String repositoryName);

	/**
	 * Access specific builds runs from the server.
	 * @param buildName the name of the build
	 * @return the artifactory build runs
	 */
	default ArtifactoryBuildRuns buildRuns(String buildName) {
		return buildRuns(buildName, null, null);
	}

	/**
	 * Access specific builds runs from the server.
	 * @param buildName the name of the build
	 * @param project the project of the build
	 * @return the artifactory build runs
	 */
	default ArtifactoryBuildRuns buildRuns(String buildName, String project) {
		return buildRuns(buildName, project, null);
	}

	/**
	 * Access specific builds runs from the server.
	 * @param buildName the name of the build
	 * @param limit the limit to the number of {@link BuildRun} items that can be returned
	 * @return the artifactory build runs
	 */
	default ArtifactoryBuildRuns buildRuns(String buildName, Integer limit) {
		return buildRuns(buildName, null, limit);
	}

	/**
	 * Access specific builds runs from the server.
	 * @param buildName the name of the build
	 * @param project the project of the build
	 * @param limit the limit to the number of {@link BuildRun} items that can be returned
	 * @return the artifactory build runs
	 */
	ArtifactoryBuildRuns buildRuns(String buildName, String project, Integer limit);

}
