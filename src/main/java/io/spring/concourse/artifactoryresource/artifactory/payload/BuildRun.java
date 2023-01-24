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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.concourse.artifactoryresource.jackson.JsonArtifactoryDateFormat;

/**
 * A reference to a build that has already run.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class BuildRun implements Comparable<BuildRun> {

	private String uri;

	@JsonArtifactoryDateFormat
	private Instant started;

	@JsonCreator
	public BuildRun(@JsonProperty("uri") String uri, @JsonProperty("started") Instant started) {
		this.uri = uri;
		this.started = started;
	}

	public String getBuildNumber() {
		return this.uri.substring(1);
	}

	public String getUri() {
		return this.uri;
	}

	public Instant getStarted() {
		return this.started;
	}

	@Override
	public int compareTo(BuildRun other) {
		return this.started.compareTo(other.started);
	}

}
