/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private final Date started;

	@JsonProperty("url")
	private final String buildUri;

	private List<BuildModule> modules;

	@JsonCreator
	public BuildInfo(@JsonProperty("name") String buildName,
			@JsonProperty("number") String buildNumber,
			@JsonProperty("agent") ContinuousIntegrationAgent continuousIntegrationAgent,
			@JsonProperty("started") Date started, @JsonProperty("url") String buildUri,
			@JsonProperty("") List<BuildModule> modules) {
		Assert.hasText(buildName, "BuildName must not be empty");
		Assert.hasText(buildNumber, "BuildNumber must not be empty");
		this.buildName = buildName;
		this.buildNumber = buildNumber;
		this.continuousIntegrationAgent = continuousIntegrationAgent;
		this.started = (started == null ? new Date() : started);
		this.buildUri = buildUri;
		this.modules = (modules == null ? Collections.emptyList()
				: Collections.unmodifiableList(new ArrayList<>(modules)));
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

	public Date getStarted() {
		return this.started;
	}

	public String getBuildUri() {
		return this.buildUri;
	}

	public List<BuildModule> getModules() {
		return this.modules;
	}

}
