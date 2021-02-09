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

package io.spring.concourse.artifactoryresource.http;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SimpleSslClientHttpRequestFactory}.
 *
 * @author Phillip Webb
 */
class SimpleSslClientHttpRequestFactoryTests {

	@Test
	void connectWithCustomSslContextFactory() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleSslClientHttpRequestFactory(
				new InsecureSslContextFactory());
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		ResponseEntity<String> response = restTemplate.getForEntity("https://self-signed.badssl.com/", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
