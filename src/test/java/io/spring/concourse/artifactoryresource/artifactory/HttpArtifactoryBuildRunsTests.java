/*
 * Copyright 2017-2020 the original author or authors.
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

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link HttpArtifactoryBuildRuns}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestClientTest(HttpArtifactory.class)
class HttpArtifactoryBuildRunsTests {

	private static final Pattern AQL_PATTERN = Pattern.compile("items.find\\((.+)\\)");

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private Artifactory artifactory;

	private ArtifactoryBuildRuns artifactoryBuildRuns;

	@BeforeEach
	void setup() {
		this.artifactoryBuildRuns = this.artifactory.server("https://repo.example.com", "admin", "password")
				.buildRuns("my-build");
	}

	@AfterEach
	void tearDown() throws Exception {
		this.customizer.getExpectationManagers().clear();
	}

	@Test
	void addAddsBuildInfo() throws Exception {
		this.server.expect(requestTo("https://repo.example.com/api/build")).andExpect(method(HttpMethod.PUT))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonContent(getResource("payload/build-info.json"))).andRespond(withSuccess());
		ContinuousIntegrationAgent agent = new ContinuousIntegrationAgent("Concourse", "3.0.0");
		BuildArtifact artifact = new BuildArtifact("jar", "a9993e364706816aba3e25717850c26c9cd0d89d",
				"900150983cd24fb0d6963f7d28e17f72", "foo.jar");
		List<BuildArtifact> artifacts = Collections.singletonList(artifact);
		List<BuildModule> modules = Collections
				.singletonList(new BuildModule("com.example.module:my-module:1.0.0-SNAPSHOT", artifacts));
		Date started = ArtifactoryDateFormat.parse("2014-09-30T12:00:19.893+0000");
		this.artifactoryBuildRuns.add("5678", "https://ci.example.com", started, agent, modules);
		this.server.verify();
	}

	@Test
	void getAllWhenBuildDoesNotExistReturnsEmptyList() {
		this.server.expect(requestTo("https://repo.example.com/api/build/my-build")).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
		List<BuildRun> runs = this.artifactoryBuildRuns.getAll();
		assertThat(runs).hasSize(0);
	}

	@Test
	void getAllReturnsBuildRuns() throws Exception {
		this.server.expect(requestTo("https://repo.example.com/api/build/my-build")).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(getResource("payload/build-runs-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = this.artifactoryBuildRuns.getAll();
		assertThat(runs).hasSize(2);
		assertThat(runs.get(0).getUri()).isEqualTo("/1234");
		assertThat(runs.get(1).getUri()).isEqualTo("/5678");
	}

	@Test
	void getRawBuildInfoReturnsBuildInfo() throws Exception {
		this.server.expect(requestTo("https://repo.example.com/api/build/my-build/5678"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(getResource("payload/build-info.json"), MediaType.APPLICATION_JSON));
		String buildInfo = this.artifactoryBuildRuns.getRawBuildInfo("5678");
		assertThat(buildInfo).isNotEmpty().contains("my-build");
	}

	@Test
	void fetchAllFetchesArtifactsCorrespondingToBuildAndRepo() throws Exception {
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN)).andExpect(aqlContent("my-build", "1234"))
				.andRespond(withSuccess(getResource("payload/deployed-artifacts.json"), MediaType.APPLICATION_JSON));
		List<DeployedArtifact> artifacts = this.artifactoryBuildRuns.getDeployedArtifacts("1234");
		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).getModifiedBy()).isEqualTo("spring");
		this.server.verify();
	}

	private RequestMatcher aqlContent(String buildName, String buildNumber) {
		return (request) -> {
			String body = ((MockClientHttpRequest) request).getBodyAsString();
			Matcher matcher = AQL_PATTERN.matcher(body);
			assertThat(matcher.matches()).isTrue();
			String actualJson = matcher.group(1);
			String expectedJson = "{\"@build.name\": \"" + buildName + "\", \"@build.number\": \"" + buildNumber
					+ "\"}";
			assertJson(expectedJson, actualJson);
		};
	}

	private RequestMatcher jsonContent(Resource expected) {
		return (request) -> {
			String actualJson = ((MockClientHttpRequest) request).getBodyAsString();
			String expectedJson = FileCopyUtils
					.copyToString(new InputStreamReader(expected.getInputStream(), Charset.forName("UTF-8")));
			assertJson(actualJson, expectedJson);
		};
	}

	private void assertJson(String actualJson, String expectedJson) throws AssertionError {
		try {
			JSONAssert.assertEquals(expectedJson, actualJson, false);
		}
		catch (JSONException ex) {
			throw new AssertionError(ex.getMessage(), ex);
		}
	}

	private Resource getResource(String path) {
		return new ClassPathResource(path, getClass());
	}

}
