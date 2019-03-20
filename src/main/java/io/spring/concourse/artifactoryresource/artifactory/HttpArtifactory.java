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

import java.io.IOException;
import java.net.URI;

import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequestFactoryWrapper;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
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
				? this.restTemplateBuilder.requestFactory(
						() -> new BasicAuthClientHttpRequestFactory(username, password))
				: this.restTemplateBuilder);
		return new HttpArtifactoryServer(uri, builder);
	}

	private static class BasicAuthClientHttpRequestFactory
			extends AbstractClientHttpRequestFactoryWrapper {

		private final String username;

		private final String password;

		protected BasicAuthClientHttpRequestFactory(String username, String password) {
			super(new ClientHttpRequestFactorySupplier().get());
			this.username = username;
			this.password = password;
		}

		@Override
		protected ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod,
				ClientHttpRequestFactory requestFactory) throws IOException {
			ClientHttpRequest request = requestFactory.createRequest(uri, httpMethod);
			request.getHeaders().setBasicAuth(this.username, this.password);
			return request;
		}

	}

}
