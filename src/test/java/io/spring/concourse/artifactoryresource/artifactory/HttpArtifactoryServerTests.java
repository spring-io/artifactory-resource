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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link HttpArtifactoryServer}.
 *
 * @author Phillip Webb
 */
@RestClientTest
class HttpArtifactoryServerTests {

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@AfterEach
	void tearDown() {
		this.customizer.getExpectationManagers().clear();
	}

	@Test
	void whenAdminIsTrueSetsAdminTrue() {
		HttpArtifactoryServer artifactoryServer = artifactoryServer(this.restTemplateBuilder.build(), true);
		assertThat(artifactoryServer.isAdmin()).isTrue();
	}

	@Test
	void whenAdminIsFalseSetsAdminFalse() {
		HttpArtifactoryServer artifactoryServer = artifactoryServer(this.restTemplateBuilder.build(), false);
		assertThat(artifactoryServer.isAdmin()).isFalse();
	}

	@Test
	void whenAdminIsNullAndIsAdmingDetectsAdminTrue() {
		RestTemplate restTemplate = this.restTemplateBuilder.build();
		this.server.expect(requestTo("https://repo.example.com/api/system/service_id"))
				.andExpect(method(HttpMethod.HEAD)).andRespond(withSuccess());
		HttpArtifactoryServer artifactoryServer = artifactoryServer(restTemplate, null);
		assertThat(artifactoryServer.isAdmin()).isTrue();
		this.server.verify();
	}

	@Test
	void whenAdminIsNullAndIsNotAdmingDetectsAdminFalse() {
		RestTemplate restTemplate = this.restTemplateBuilder.build();
		this.server.expect(requestTo("https://repo.example.com/api/system/service_id"))
				.andExpect(method(HttpMethod.HEAD)).andRespond(withForbiddenRequest());
		HttpArtifactoryServer artifactoryServer = artifactoryServer(restTemplate, null);
		assertThat(artifactoryServer.isAdmin()).isFalse();
		this.server.verify();
	}

	private HttpArtifactoryServer artifactoryServer(RestTemplate restTemplate, Boolean admin) {
		return new HttpArtifactoryServer(restTemplate, "https://repo.example.com", null, admin);
	}

}
