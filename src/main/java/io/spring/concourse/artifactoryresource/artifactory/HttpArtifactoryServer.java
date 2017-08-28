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

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * Default {@link ArtifactoryServer} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class HttpArtifactoryServer implements ArtifactoryServer {

	private final String uri;

	private final RestTemplate restTemplate;

	public HttpArtifactoryServer(String uri, RestTemplateBuilder restTemplateBuilder) {
		this.uri = uri;
		this.restTemplate = restTemplateBuilder.build();
	}

	@Override
	public ArtifactoryRepository repository(String repositoryName) {
		return new HttpArtifactoryRepository(this.restTemplate, this.uri, repositoryName);
	}

	@Override
	public ArtifactoryBuildRuns buildRuns(String buildName) {
		return new HttpArtifactoryBuildRuns(this.restTemplate, this.uri, buildName);
	}

}
