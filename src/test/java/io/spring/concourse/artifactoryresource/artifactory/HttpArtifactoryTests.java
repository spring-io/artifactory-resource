/*
 * Copyright 2017-2024 the original author or authors.
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

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HttpArtifactory}.
 *
 * @author Madhura Bhave
 * @author Gabriel Petrovay
 */
class HttpArtifactoryTests {

	/**
	 *
	 */
	private static final String URI = "https://example.com";

	private RestTemplateBuilder builder = new RestTemplateBuilder();

	private HttpArtifactory artifactory;

	@BeforeEach
	void setup() {
		this.artifactory = new HttpArtifactory(this.builder);
	}

	@Test
	void serverWhenNoUsernameReturnsServer() {
		ArtifactoryServer server = this.artifactory.server(URI, null, null, null, null, false);
		RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(server, "restTemplate");
		List<?> interceptors = (List<?>) ReflectionTestUtils.getField(restTemplate, "interceptors");
		assertThat(interceptors.size()).isEqualTo(0);
	}

	@Test
	void serverWithCredentialsReturnsServerWithCredentials() throws Exception {
		ArtifactoryServer server = this.artifactory.server(URI, "admin", "password", null, null, false);
		RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(server, "restTemplate");
		ClientHttpRequest request = restTemplate.getRequestFactory().createRequest(new URI("http://localhost"),
				HttpMethod.GET);
		assertThat(request.getHeaders()).containsKey(HttpHeaders.AUTHORIZATION);
	}

	@Test
	void serverWithProxyReturnsServerWithProxy() throws Exception {
		Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 8080));
		ArtifactoryServer server = this.artifactory.server(URI, "admin", "password", proxy, null, false);
		RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(server, "restTemplate");
		ClientHttpRequest request = restTemplate.getRequestFactory().createRequest(new URI("http://localhost"),
				HttpMethod.GET);
		HttpURLConnection connection = (HttpURLConnection) ReflectionTestUtils.getField(request, "connection");
		assertThat(connection.usingProxy()).isTrue();
	}

}
