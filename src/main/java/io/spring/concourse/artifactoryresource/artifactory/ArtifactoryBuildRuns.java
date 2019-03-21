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

package io.spring.concourse.artifactoryresource.artifactory;

import java.util.Date;
import java.util.List;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;

import org.springframework.util.Assert;

/**
 * Access to artifactory build runs.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public interface ArtifactoryBuildRuns {

	/**
	 * Add a new build run.
	 * @param buildNumber the build number
	 * @param buildUri the build URL
	 * @param modules the modules for the build run
	 */
	default void add(String buildNumber, String buildUri, List<BuildModule> modules) {
		add(buildNumber, buildUri, null, new Date(), modules);
	}

	/**
	 * Add a new build run.
	 * @param buildNumber the build number
	 * @param buildUri the build URL
	 * @param continuousIntegrationAgent the CI Agent
	 * @param modules the modules for the build run
	 */
	default void add(String buildNumber, String buildUri,
			ContinuousIntegrationAgent continuousIntegrationAgent,
			List<BuildModule> modules) {
		add(buildNumber, buildUri, continuousIntegrationAgent, new Date(), modules);
	}

	/**
	 * Add a new build run.
	 * @param buildNumber the build number
	 * @param buildUri the build URL
	 * @param continuousIntegrationAgent the CI Agent
	 * @param started the date the build was started
	 * @param modules the modules for the build run
	 */
	void add(String buildNumber, String buildUri,
			ContinuousIntegrationAgent continuousIntegrationAgent, Date started,
			List<BuildModule> modules);

	/**
	 * Return all previous build runs.
	 * @return the build runs
	 */
	List<BuildRun> getAll();

	/**
	 * Return a string containing the build-info JSON as stored on the server.
	 * @param buildNumber the build number
	 * @return a string containing the build-info JSON
	 */
	String getRawBuildInfo(String buildNumber);

	/**
	 * Return all artifacts that were deployed for the specified build run.
	 * @param buildRun the build run
	 * @return the deployed artifacts
	 */
	default List<DeployedArtifact> getDeployedArtifacts(BuildRun buildRun) {
		Assert.notNull(buildRun, "BuildRun must not be null");
		return getDeployedArtifacts(buildRun.getBuildNumber());
	}

	/**
	 * Return all artifacts that were deployed for the specified build number.
	 * @param buildNumber the build number
	 * @return the deployed artifacts
	 */
	List<DeployedArtifact> getDeployedArtifacts(String buildNumber);

}
