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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;

/**
 * The CI agent information included in {@link BuildInfo}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ContinuousIntegrationAgent {

	private final String name;

	private final String version;

	public ContinuousIntegrationAgent() {
		this("Concourse", null);
	}

	@JsonCreator
	public ContinuousIntegrationAgent(@JsonProperty("name") String name, @JsonProperty("version") String version) {
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return this.name;
	}

	public String getVersion() {
		return this.version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ContinuousIntegrationAgent other = (ContinuousIntegrationAgent) obj;
		return Objects.equals(this.name, other.name) && Objects.equals(this.version, other.version);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.version);
	}

	@Override
	public String toString() {
		return this.name + ":" + ((this.version != null) ? this.version : "unknown");
	}

}
