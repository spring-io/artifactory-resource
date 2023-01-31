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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildInfo;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRunsResponse;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifactsResponse;
import io.spring.concourse.artifactoryresource.artifactory.payload.SearchQueryResponse;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default {@link ArtifactoryBuildRuns} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class HttpArtifactoryBuildRuns implements ArtifactoryBuildRuns {

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

	private final RestTemplate restTemplate;

	private final String uri;

	private final String buildName;

	public HttpArtifactoryBuildRuns(RestTemplate restTemplate, String uri, String buildName) {
		this.restTemplate = restTemplate;
		this.uri = uri;
		this.buildName = buildName;
	}

	@Override
	public void add(String buildNumber, ContinuousIntegrationAgent continuousIntegrationAgent, Instant started,
			String buildUri, Map<String, String> properties, List<BuildModule> modules) {
		add(new BuildInfo(this.buildName, buildNumber, continuousIntegrationAgent, started, buildUri, properties,
				modules));
	}

	private void add(BuildInfo buildInfo) {
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(this.uri).path("api/build").build();
		URI uri = uriComponents.encode().toUri();
		RequestEntity<BuildInfo> request = RequestEntity.put(uri).contentType(MediaType.APPLICATION_JSON)
				.body(buildInfo);
		ResponseEntity<Void> exchange = this.restTemplate.exchange(request, Void.class);
		exchange.getBody();
	}

	@Override
	public List<BuildRun> getAll() {
		return getBuildRuns(null);
	}

	@Override
	public List<BuildRun> getStartedOnOrAfter(Instant timestamp) {
		Assert.notNull(timestamp, "Timestamp must not be null");
		return getBuildRuns(timestamp);
	}

	private List<BuildRun> getBuildRuns(Instant startedOnOrAfter) {
		Json critera = Json.of("name", this.buildName);
		if (startedOnOrAfter != null) {
			String formattedStartTime = TIMESTAMP_FORMATTER.format(startedOnOrAfter.atOffset(ZoneOffset.UTC));
			critera.and("started", Json.of("$gte", formattedStartTime));
		}
		String query = "builds.find(%s)".formatted(critera);
		return search(query, BuildRunsResponse.class).getResults();
	}

	@Override
	public String getRawBuildInfo(String buildNumber) {
		Assert.hasText(buildNumber, "BuildNumber must not be empty");
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(this.uri)
				.path("api/build/{buildName}/{buildNumber}").buildAndExpand(this.buildName, buildNumber);
		URI uri = uriComponents.encode().toUri();
		return this.restTemplate.getForObject(uri, String.class);
	}

	@Override
	public List<DeployedArtifact> getDeployedArtifacts(String buildNumber) {
		Assert.notNull(buildNumber, "Build number must not be null");
		Json criteria = Json.of("@build.name", this.buildName).and("@build.number", buildNumber);
		String query = "items.find(%s)".formatted(criteria);
		return search(query, DeployedArtifactsResponse.class).getResults();
	}

	private <T extends SearchQueryResponse<?>> T search(String query, Class<T> responseType) {
		URI uri = UriComponentsBuilder.fromUriString(this.uri).path("/api/search/aql").build().encode().toUri();
		RequestEntity<String> request = RequestEntity.post(uri).contentType(MediaType.TEXT_PLAIN).body(query);
		return this.restTemplate.exchange(request, responseType).getBody();
	}

	/**
	 * Simple JSON builder support class.
	 */
	static class Json {

		private final StringBuilder json = new StringBuilder();

		Json and(String field, Object value) {
			this.json.append((!this.json.isEmpty()) ? ", " : "");
			appendJson(field);
			this.json.append(" : ");
			appendJson(value);
			return this;
		}

		private void appendJson(Object value) {
			if (value instanceof Json) {
				this.json.append(value);
			}
			else {
				this.json.append("\"%s\"".formatted(value));
			}
		}

		@Override
		public String toString() {
			return "{%s}".formatted(this.json);
		}

		static Json of(String field, Object value) {
			return new Json().and(field, value);
		}

	}

}
