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

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link HttpArtifactoryBuildRuns}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestClientTest(HttpArtifactory.class)
class HttpArtifactoryBuildRunsTests {

	private static final String URI = "https://repo.example.com";

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private Artifactory artifactory;

	@AfterEach
	void tearDown() {
		this.customizer.getExpectationManagers().clear();
	}

	@Test
	void addAddsBuildInfo() {
		ArtifactoryBuildRuns buildRuns = buildRuns();
		this.server.expect(requestTo("https://repo.example.com/api/build")).andExpect(method(HttpMethod.PUT))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonContent(getResource("payload/build-info.json"))).andRespond(withSuccess());
		ContinuousIntegrationAgent agent = new ContinuousIntegrationAgent("Concourse", "3.0.0");
		BuildAgent buildAgent = new BuildAgent("Concourse", "3.0.0");
		BuildArtifact artifact = new BuildArtifact("jar", "a9993e364706816aba3e25717850c26c9cd0d89d",
				"900150983cd24fb0d6963f7d28e17f72", "foo.jar");
		List<BuildArtifact> artifacts = Collections.singletonList(artifact);
		List<BuildModule> modules = Collections
				.singletonList(new BuildModule("com.example.module:my-module:1.0.0-SNAPSHOT", artifacts));
		Instant started = ArtifactoryDateFormat.parse("2014-09-30T12:00:19.893Z");
		Map<String, String> properties = Collections.singletonMap("made-by", "concourse");
		buildRuns.add(BuildNumber.of("5678"), agent, buildAgent, started, "https://ci.example.com", properties,
				modules);
		this.server.verify();
	}

	@Test
	void getAllWhenAdminAndBuildDoesNotExistReturnsEmptyList() {
		ArtifactoryBuildRuns buildRuns = buildRuns(true);
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithFindAllBuildsQuery("my-build")).andRespond(withSuccess(
						getResource("payload/build-runs-aql-missing-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll(null);
		assertThat(runs).hasSize(0);
	}

	@Test
	void getAllWhenNonAdminAndBuildDoesNotExistReturnsEmptyList() {
		ArtifactoryBuildRuns buildRuns = buildRuns(false);
		String url = "https://repo.example.com/api/build/my-build";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
				withSuccess(getResource("payload/build-runs-rest-missing-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll(null);
		assertThat(runs).hasSize(0);
	}

	@Test
	void getAllWhenAdminReturnsBuildRuns() {
		ArtifactoryBuildRuns buildRuns = buildRuns(true);
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithFindAllBuildsQuery("my-build")).andRespond(
						withSuccess(getResource("payload/build-runs-aql-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll(null);
		assertThat(runs).hasSize(2);
		assertThat(runs.get(0).getBuildNumber()).isEqualTo("my-1234");
		assertThat(runs.get(1).getBuildNumber()).isEqualTo("my-5678");
	}

	@Test
	void getAllWhenNonAdminReturnsBuildRuns() {
	}

	@Test
	void getAllWhenAdminAndHasLimitReturnsBuildRuns() {
		ArtifactoryBuildRuns buildRuns = artifactoryServer(true).buildRuns("my-build", 2);
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithFindAllBuildsQuery("my-build")).andExpect(bodyWithContent(".limit(2)")).andRespond(
						withSuccess(getResource("payload/build-runs-aql-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll(null);
		assertThat(runs).hasSize(2);
	}

	@Test
	void getAllWhenNonAdminAndHasLimitReturnsBuildRuns() {
		ArtifactoryBuildRuns buildRuns = artifactoryServer(false).buildRuns("my-build", 2);
		String url = "https://repo.example.com/api/build/my-build";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
				withSuccess(getResource("payload/build-runs-rest-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll(null);
		assertThat(runs).hasSize(2);
	}

	@Test
	void getAllWhenAdminAndHasBuildNumberPrefixReturnsBuildRuns() {
		ArtifactoryBuildRuns buildRuns = buildRuns(true);
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithContent("\"number\" : {\"$match\" : \"my-*\"}")).andRespond(
						withSuccess(getResource("payload/build-runs-aql-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll("my-");
		assertThat(runs).hasSize(2);
	}

	@Test
	void getAllWhenNonAdminAndHasBuildNumberPrefixReturnsBuildRuns() {
		ArtifactoryBuildRuns buildRuns = buildRuns(false);
		String url = "https://repo.example.com/api/build/my-build";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
				withSuccess(getResource("payload/build-runs-rest-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getAll("my-");
		assertThat(runs).hasSize(2);
	}

	private RequestMatcher bodyWithFindAllBuildsQuery(String buildName) {
		return bodyWithQuery("builds", """
				{"name": "%s"}""".formatted(buildName));
	}

	@Test
	void getStartedOnOrAfterWhenAdminReturnsBuilds() {
		ArtifactoryBuildRuns buildRuns = buildRuns(true);
		String url = "https://repo.example.com/api/search/aql";
		String started = "2014-09-30T12:00:19.893Z";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithFindBuildsStartedOnOrAfterQuery("my-build", started)).andRespond(
						withSuccess(getResource("payload/build-runs-aql-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getStartedOnOrAfter(null, ArtifactoryDateFormat.parse(started));
		assertThat(runs).hasSize(2);
		assertThat(runs.get(0).getBuildNumber()).isEqualTo("my-1234");
		assertThat(runs.get(1).getBuildNumber()).isEqualTo("my-5678");
	}

	@Test
	void getStartedOnOrAfterWhenNonAdminReturnsBuilds() {
		ArtifactoryBuildRuns buildRuns = buildRuns(false);
		String url = "https://repo.example.com/api/build/my-build";
		String started = "2014-09-30T12:00:19.893Z";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET)).andRespond(
				withSuccess(getResource("payload/build-runs-rest-response.json"), MediaType.APPLICATION_JSON));
		List<BuildRun> runs = buildRuns.getStartedOnOrAfter(null, ArtifactoryDateFormat.parse(started));
		assertThat(runs).hasSize(2);
		assertThat(runs.get(0).getBuildNumber()).isEqualTo("my-5678");
		assertThat(runs.get(1).getBuildNumber()).isEqualTo("other-5678");
	}

	private RequestMatcher bodyWithFindBuildsStartedOnOrAfterQuery(String buildName, String started) {
		return bodyWithQuery("builds", """
				{"name": "%s", "started": {"$gte": "%s"}}""".formatted(buildName, started));
	}

	@Test
	void getRawBuildInfoReturnsBuildInfo() {
		ArtifactoryBuildRuns buildRuns = buildRuns();
		this.server.expect(requestTo("https://repo.example.com/api/build/my-build/5678"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(getResource("payload/build-info.json"), MediaType.APPLICATION_JSON));
		String buildInfo = buildRuns.getRawBuildInfo(BuildNumber.of("5678"));
		assertThat(buildInfo).isNotEmpty().contains("my-build");
	}

	@Test
	void fetchAllFetchesArtifactsCorrespondingToBuildAndRepo() {
		ArtifactoryBuildRuns buildRuns = buildRuns();
		String url = "https://repo.example.com/api/search/aql";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.TEXT_PLAIN))
				.andExpect(bodyWithFindItemsQuery("my-build", "1234"))
				.andRespond(withSuccess(getResource("payload/deployed-artifacts.json"), MediaType.APPLICATION_JSON));
		List<DeployedArtifact> artifacts = buildRuns.getDeployedArtifacts(BuildNumber.of("1234"));
		assertThat(artifacts).hasSize(1);
		assertThat(artifacts.get(0).getModifiedBy()).isEqualTo("spring");
		this.server.verify();
	}

	private RequestMatcher bodyWithFindItemsQuery(String buildName, String buildNumber) {
		return bodyWithQuery("items", """
				{"@build.name": "%s", "@build.number": "%s"}""".formatted(buildName, buildNumber));
	}

	private RequestMatcher bodyWithQuery(String entity, String expectedCriteria) {
		Pattern pattern = Pattern.compile(entity + "\\.find\\((.+)\\)", Pattern.DOTALL);
		return (request) -> {
			String body = ((MockClientHttpRequest) request).getBodyAsString();
			Matcher matcher = pattern.matcher(body);
			assertThat(body).matches(pattern);
			assertThat(matcher.matches());
			String actualCriteria = matcher.group(1);
			assertJson(expectedCriteria, actualCriteria);
		};
	}

	private RequestMatcher bodyWithContent(String content) {
		return (request) -> {
			String body = ((MockClientHttpRequest) request).getBodyAsString();
			assertThat(body).contains(content);
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

	private ArtifactoryBuildRuns buildRuns() {
		return buildRuns(false);
	}

	private ArtifactoryBuildRuns buildRuns(boolean admin) {
		return artifactoryServer(admin).buildRuns("my-build");
	}

	private ArtifactoryServer artifactoryServer(boolean admin) {
		return this.artifactory.server(URI, "admin", "password", null, null, admin);
	}

}
