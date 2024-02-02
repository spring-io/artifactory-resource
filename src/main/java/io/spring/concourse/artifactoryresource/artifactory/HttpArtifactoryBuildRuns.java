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

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildInfo;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRunsRestResponse;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRunsSearchQueryResponse;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifactsSearchQueryResponse;
import io.spring.concourse.artifactoryresource.artifactory.payload.SearchQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default {@link ArtifactoryBuildRuns} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public final class HttpArtifactoryBuildRuns implements ArtifactoryBuildRuns {

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
		.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

	private static final Logger logger = LoggerFactory.getLogger(HttpArtifactoryBuildRuns.class);

	private final RestTemplate restTemplate;

	private final String uri;

	private final String buildName;

	private final String project;

	private final Integer limit;

	private final BuildRunsProvider buildRunsProvider;

	public HttpArtifactoryBuildRuns(RestTemplate restTemplate, String uri, String buildName, String project,
			Integer limit, boolean admin) {
		this.restTemplate = restTemplate;
		this.uri = uri;
		this.buildName = buildName;
		this.project = project;
		this.limit = limit;
		this.buildRunsProvider = (!admin) ? new RestBuildRunsProvider()
				: new ArtifactoryQueryLanguageBuildRunsProvider();
	}

	@Override
	public void add(BuildNumber buildNumber, ContinuousIntegrationAgent continuousIntegrationAgent, Instant started,
			String buildUri, Map<String, String> properties, List<BuildModule> modules) {
		logger.debug("Adding {} to from CI agent {}", buildNumber, continuousIntegrationAgent);
		add(new BuildInfo(this.buildName, buildNumber.toString(), continuousIntegrationAgent, started, buildUri,
				properties, modules));
	}

	private void add(BuildInfo buildInfo) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(this.uri).path("api/build");
		if (this.project != null) {
			logger.debug("Publishing to project {}", this.project);
			builder = builder.queryParam("project", this.project);
		}
		UriComponents uriComponents = builder.build();
		URI uri = uriComponents.encode().toUri();
		logger.info("Publishing build info to {}", uri);
		RequestEntity<BuildInfo> request = RequestEntity.put(uri)
			.contentType(MediaType.APPLICATION_JSON)
			.body(buildInfo);
		ResponseEntity<Void> exchange = this.restTemplate.exchange(request, Void.class);
		exchange.getBody();
	}

	@Override
	public List<BuildRun> getAll(String buildNumberPrefix) {
		return this.buildRunsProvider.getBuildRuns(buildNumberPrefix, null);
	}

	@Override
	public List<BuildRun> getStartedOnOrAfter(String buildNumberPrefix, Instant timestamp) {
		Assert.notNull(timestamp, "Timestamp must not be null");
		return this.buildRunsProvider.getBuildRuns(buildNumberPrefix, timestamp);
	}

	@Override
	public String getRawBuildInfo(BuildNumber buildNumber) {
		logger.debug("Getting raw build info for {}", buildNumber);
		Assert.notNull(buildNumber, "BuildNumber must not be null");
		UriComponents uriComponents = UriComponentsBuilder.fromUriString(this.uri)
			.path("api/build/{buildName}/{buildNumber}")
			.buildAndExpand(this.buildName, buildNumber);
		URI uri = uriComponents.encode().toUri();
		return this.restTemplate.getForObject(uri, String.class);
	}

	@Override
	public List<DeployedArtifact> getDeployedArtifacts(BuildNumber buildNumber) {
		logger.debug("Getting deployed artifacts for {}", buildNumber);
		Assert.notNull(buildNumber, "Build number must not be null");
		Json criteria = Json.of("@build.name", this.buildName).and("@build.number", buildNumber);
		String query = "items.find(%s)".formatted(criteria);
		return search(query, DeployedArtifactsSearchQueryResponse.class).getResults();
	}

	protected <T extends SearchQueryResponse<?>> T search(String query, Class<T> responseType) {
		logger.debug("Searching with AQL {}", query);
		URI uri = UriComponentsBuilder.fromUriString(this.uri).path("/api/search/aql").build().encode().toUri();
		RequestEntity<String> request = RequestEntity.post(uri).contentType(MediaType.TEXT_PLAIN).body(query);
		return this.restTemplate.exchange(request, responseType).getBody();
	}

	/**
	 * Strategy interface used to provide build runs. Allows us to switch to an optimal
	 * AQL implementation if the user has admin rights.
	 */
	private interface BuildRunsProvider {

		/**
		 * Get build runs.
		 * @param buildNumberPrefix the build number prefix or {@code null}
		 * @param startedOnOrAfter the started on or after date or {@code null}
		 * @return a list of build runs
		 */
		List<BuildRun> getBuildRuns(String buildNumberPrefix, Instant startedOnOrAfter);

	}

	/**
	 * {@link BuildRunsProvider} backed by AQL that can be used when the user is an admin.
	 */
	private class ArtifactoryQueryLanguageBuildRunsProvider implements BuildRunsProvider {

		@Override
		public List<BuildRun> getBuildRuns(String buildNumberPrefix, Instant startedOnOrAfter) {
			String buildName = HttpArtifactoryBuildRuns.this.buildName;
			Integer limit = HttpArtifactoryBuildRuns.this.limit;
			logger.debug("Using AQL to get build runs with prefix {} started on or after {}", buildNumberPrefix,
					startedOnOrAfter);
			Json critera = Json.of("name", buildName);
			if (startedOnOrAfter != null) {
				String formattedStartTime = TIMESTAMP_FORMATTER.format(startedOnOrAfter.atOffset(ZoneOffset.UTC));
				critera.and("started", Json.of("$gte", formattedStartTime));
			}
			if (StringUtils.hasText(buildNumberPrefix)) {
				critera.and("number", Json.of("$match", buildNumberPrefix + "*"));
			}
			String query = "builds.find(%s)".formatted(critera);
			if (limit != null && limit > 0) {
				query += (".limit(%s)".formatted(limit));
			}
			BuildRunsSearchQueryResponse response = search(query, BuildRunsSearchQueryResponse.class);
			return Collections.unmodifiableList(response.getResults());
		}

	}

	/**
	 * {@link BuildRunsProvider} backed by the standard REST API that can be used when the
	 * user is not an admin.
	 */
	private class RestBuildRunsProvider implements BuildRunsProvider {

		@Override
		public List<BuildRun> getBuildRuns(String buildNumberPrefix, Instant startedOnOrAfter) {
			logger.debug("Using REST call to get build runs with prefix {} started on or after {}", buildNumberPrefix,
					startedOnOrAfter);
			RestTemplate restTemplate = HttpArtifactoryBuildRuns.this.restTemplate;
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(HttpArtifactoryBuildRuns.this.uri)
				.path("api/build/{buildName}");
			if (HttpArtifactoryBuildRuns.this.project != null) {
				builder = builder.queryParam("project", HttpArtifactoryBuildRuns.this.project);
			}
			UriComponents uriComponents = builder.buildAndExpand(HttpArtifactoryBuildRuns.this.buildName);
			URI uri = uriComponents.encode().toUri();
			List<BuildRun> all = restTemplate.getForObject(uri, BuildRunsRestResponse.class).getBuildsRuns();
			return filterAndLimit(all, buildNumberPrefix, startedOnOrAfter);
		}

		private List<BuildRun> filterAndLimit(List<BuildRun> all, String buildNumberPrefix, Instant startedOnOrAfter) {
			List<BuildRun> result = limit(all);
			logger.debug("Found {} build run candidates", result.size());
			if (buildNumberPrefix == null && startedOnOrAfter == null) {
				return result;
			}
			result = result.stream()
				.filter((buildRun) -> hasPrefix(buildRun, buildNumberPrefix))
				.filter((buildRun) -> isStartedOnOrAfter(buildRun, startedOnOrAfter))
				.toList();
			logger.debug("Returning {} build run results after filtering", result.size());
			return result;
		}

		private List<BuildRun> limit(List<BuildRun> all) {
			Integer limit = HttpArtifactoryBuildRuns.this.limit;
			if (limit == null || limit <= 0) {
				return all;
			}
			logger.debug("Limiting build runs to {}", limit);
			return all.stream().sorted(Comparator.reverseOrder()).limit(limit).toList();
		}

		private boolean hasPrefix(BuildRun buildRun, String buildNumberPrefix) {
			boolean result = !StringUtils.hasText(buildNumberPrefix)
					|| buildRun.getBuildNumber().startsWith(buildNumberPrefix);
			logger.trace("Checked if {} starts with {} [{}]", buildRun, buildNumberPrefix, result);
			return result;
		}

		private boolean isStartedOnOrAfter(BuildRun buildRun, Instant startedOnOrAfter) {
			boolean result = startedOnOrAfter == null || buildRun.getStarted().compareTo(startedOnOrAfter) >= 0;
			logger.trace("Checked if {} was started on or after {} [{}]", buildRun, startedOnOrAfter, result);
			return result;
		}

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
