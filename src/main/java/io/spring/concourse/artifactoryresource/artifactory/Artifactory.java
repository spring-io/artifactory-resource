/*
 * Copyright 2017-2021 the original author or authors.
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

import java.net.Proxy;
import java.time.Duration;

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
	 * @param proxy the proxy to use or {@code null}
	 * @return an {@link ArtifactoryServer}
	 */
	default ArtifactoryServer server(String uri, String username, String password, Proxy proxy) {
		return server(uri, username, password, proxy, null);
	}

	/**
	 * Return an {@link ArtifactoryServer} for the specified connection details.
	 * @param uri the server URI
	 * @param username the connection username
	 * @param password the connection password
	 * @param proxy the proxy to use or {@code null}
	 * @param retryDelay the delay between retries
	 * @return an {@link ArtifactoryServer}
	 */
	ArtifactoryServer server(String uri, String username, String password, Proxy proxy, Duration retryDelay);

}
