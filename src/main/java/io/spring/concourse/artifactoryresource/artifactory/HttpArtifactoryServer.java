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

import java.net.URI;
import java.time.Duration;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default {@link ArtifactoryServer} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class HttpArtifactoryServer implements ArtifactoryServer {

	private final String uri;

	private final RestTemplate restTemplate;

	private final Duration retryDelay;

	private final boolean admin;

	HttpArtifactoryServer(RestTemplate restTemplate, String uri, Duration retryDelay, Boolean admin) {
		this.uri = uri;
		this.restTemplate = restTemplate;
		this.retryDelay = retryDelay;
		this.admin = (admin != null) ? admin : detectAdmin();
	}

	private boolean detectAdmin() {
		// Check system/service_id which requires admin rights
		URI uri = UriComponentsBuilder.fromUriString(this.uri).path("api/system/service_id").build().encode().toUri();
		try {
			this.restTemplate.headForHeaders(uri);
		}
		catch (HttpClientErrorException.Forbidden ex) {
			return false;
		}
		return true;
	}

	@Override
	public ArtifactoryRepository repository(String repositoryName) {
		return new HttpArtifactoryRepository(this.restTemplate, this.uri, repositoryName, this.retryDelay);
	}

	@Override
	public ArtifactoryBuildRuns buildRuns(String buildName, Integer limit) {
		return new HttpArtifactoryBuildRuns(this.restTemplate, this.uri, buildName, limit, this.admin);
	}

	boolean isAdmin() {
		return this.admin;
	}

}
