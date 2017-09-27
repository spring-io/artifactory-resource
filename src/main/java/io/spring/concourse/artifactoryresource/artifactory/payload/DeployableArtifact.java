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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.util.Map;

import org.springframework.core.io.Resource;

/**
 * A single artifact that can be deployed.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public interface DeployableArtifact {

	/**
	 * Return the path of the artifact starting with {@code /}, for example
	 * {@code /com/example/foo/1.0.0-SNAPSHOT/bar.jar}.
	 * @return the path of the artifact
	 */
	String getPath();

	/**
	 * Return the contents of the underlying artifact file.
	 * @return the contents of the artifact
	 */
	Resource getContent();

	/**
	 * Return the size of the contents in bytes.
	 * @return the size
	 */
	long getSize();

	/**
	 * Return any property meta-data that is attached to the artifact.
	 * @return the property meta-data
	 */
	Map<String, String> getProperties();

	/**
	 * Return the checksums (SHA1, MD5) for the artifact.
	 * @return the checksums
	 */
	Checksums getChecksums();

}
