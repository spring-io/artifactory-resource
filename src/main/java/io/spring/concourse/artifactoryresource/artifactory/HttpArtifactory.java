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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Default {@link Artifactory} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Service
public class HttpArtifactory implements Artifactory {

	private final RestTemplateBuilder restTemplateBuilder;

	HttpArtifactory(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplateBuilder = restTemplateBuilder;
	}

	@Override
	public ArtifactoryServer server(String uri, String username, String password) {
		if (!uri.endsWith("/")) {
			uri += '/';
		}
		RestTemplateBuilder builder = (StringUtils.hasText(username)
				? this.restTemplateBuilder.basicAuthorization(username, password)
				: this.restTemplateBuilder);
		return new HttpArtifactoryServer(uri, builder);
	}

}
