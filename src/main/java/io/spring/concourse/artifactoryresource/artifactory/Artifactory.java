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

package io.spring.concourse.artifactoryresource.artifactory;

/**
 * Interface providing access to Artifactory.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Gabriel Petrovay
 * @see HttpArtifactory
 */
public interface Artifactory {

	/**
	 * Return an {@link ArtifactoryServer} for the specified connection details.
	 * @param uri the server URI
	 * @param username the connection username
	 * @param password the connection password
	 * @param proxyHost the proxy host name
	 * @param proxyPort the proxy port
	 * @return an {@link ArtifactoryServer}
	 */
	ArtifactoryServer server(String uri, String username, String password, String proxyHost, int proxyPort);

}
