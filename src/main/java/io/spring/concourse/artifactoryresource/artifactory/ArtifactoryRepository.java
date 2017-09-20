/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.concourse.artifactoryresource.artifactory;

import java.io.File;

import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;

/**
 * Access to an artifactory repository.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public interface ArtifactoryRepository {

	/**
	 * Deploy the specified artifact to the repository.
	 * @param artifact the artifact to deploy
	 */
	void deploy(DeployableArtifact artifact);

	/**
	 * Download the specified artifact to the given destination.
	 * @param artifact the artifacts to download
	 * @param destination the destination folder.
	 */
	default void download(DeployedArtifact artifact, File destination) {
		download(artifact.getPath() + "/" + artifact.getName(), destination);
	}

	/**
	 * Download the specified artifact to the given destination.
	 * @param path the path of the artifact to download
	 * @param destination the destination folder.
	 */
	void download(String path, File destination);

}
