/*
 * Copyright 2017-2019 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Request to the {@code "/opt/resource/check"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class CheckRequest {

	private final Source source;

	private final Version version;

	@JsonCreator
	public CheckRequest(@JsonProperty("source") Source source, @JsonProperty("version") Version version) {
		Assert.notNull(source, "Source must not be null");
		this.source = source;
		this.version = version;
	}

	public Source getSource() {
		return this.source;
	}

	public Version getVersion() {
		return this.version;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("source", this.source).append("version", this.version).toString();
	}

}
