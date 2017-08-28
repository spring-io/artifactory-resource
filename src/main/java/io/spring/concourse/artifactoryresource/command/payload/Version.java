/*
 * Copyright 2017 the original author or authors.
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

package io.spring.concourse.artifactoryresource.command.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonCreator
	public Version(@JsonProperty("build_number") String buildNumber) {
		Assert.hasText(buildNumber, "Build Number must not be empty");
		this.buildNumber = buildNumber;
	}

	public String getBuildNumber() {
		return this.buildNumber;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("buildNumber", this.buildNumber)
				.toString();
	}

}
