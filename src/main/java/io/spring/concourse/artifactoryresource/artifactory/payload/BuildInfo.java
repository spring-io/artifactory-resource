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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.concourse.artifactoryresource.jackson.JsonArtifactoryDateFormat;

import org.springframework.util.Assert;

/**
 * Build information for artifactory.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class BuildInfo {

	@JsonProperty("name")
	private final String buildName;

	@JsonProperty("number")
	private final String buildNumber;

	@JsonProperty("agent")
	private final ContinuousIntegrationAgent continuousIntegrationAgent;

	@JsonArtifactoryDateFormat
	private final Instant started;

	@JsonProperty("url")
	private final String buildUri;

	@JsonProperty("properties")
	@JsonInclude(Include.NON_NULL)
	private final Map<String, String> properties;

	@JsonProperty("modules")
	private List<BuildModule> modules;

	@JsonCreator
	public BuildInfo(@JsonProperty("name") String buildName, @JsonProperty("number") String buildNumber,
			@JsonProperty("agent") ContinuousIntegrationAgent continuousIntegrationAgent,
			@JsonProperty("started") Instant started, @JsonProperty("url") String buildUri,
			@JsonProperty("properties") Map<String, String> properties,
			@JsonProperty("modules") List<BuildModule> modules) {
		Assert.hasText(buildName, "BuildName must not be empty");
		Assert.hasText(buildNumber, "BuildNumber must not be empty");
		this.buildName = buildName;
		this.buildNumber = buildNumber;
		this.continuousIntegrationAgent = continuousIntegrationAgent;
		this.started = (started != null) ? started : Instant.now();
		this.buildUri = buildUri;
		this.properties = (properties != null) ? Collections.unmodifiableMap(new LinkedHashMap<>(properties))
				: Collections.emptyMap();
		this.modules = (modules != null) ? Collections.unmodifiableList(new ArrayList<>(modules))
				: Collections.emptyList();
	}

	public String getBuildName() {
		return this.buildName;
	}

	public String getBuildNumber() {
		return this.buildNumber;
	}

	public ContinuousIntegrationAgent getContinuousIntegrationAgent() {
		return this.continuousIntegrationAgent;
	}

	public Instant getStarted() {
		return this.started;
	}

	public String getBuildUri() {
		return this.buildUri;
	}

	public List<BuildModule> getModules() {
		return this.modules;
	}

}
