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

package io.spring.concourse.artifactoryresource.command.payload;

import java.time.Instant;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.concourse.artifactoryresource.jackson.JsonIsoDateFormat;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * The version payload detailing a single build version. Can be used as both input and
 * output.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class Version {

	@JsonProperty("build_number")
	private final String buildNumber;

	@JsonIsoDateFormat
	private Instant started;

	@JsonCreator
	public Version(@JsonProperty("build_number") String buildNumber, @JsonProperty("started") Instant started) {
		Assert.hasText(buildNumber, "Build Number must not be empty");
		this.buildNumber = buildNumber;
		this.started = started;
	}

	public String getBuildNumber() {
		return this.buildNumber;
	}

	public Instant getStarted() {
		return this.started;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Version other = (Version) obj;
		return Objects.equals(this.buildNumber, other.buildNumber) && Objects.equals(this.started, other.started);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.buildNumber, this.started);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("buildNumber", this.buildNumber).append("buildTimestamp", this.started)
				.toString();
	}

}
