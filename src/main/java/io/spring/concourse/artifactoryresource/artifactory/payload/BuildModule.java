/*
 * Copyright 2017-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;

/**
 * A single module included in {@link BuildInfo}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class BuildModule {

	private final String id;

	private final List<BuildArtifact> artifacts;

	@JsonCreator
	public BuildModule(@JsonProperty("id") String id,
			@JsonProperty("artifacts") List<BuildArtifact> artifacts) {
		Assert.hasText(id, "ID must not be empty");
		this.id = id;
		this.artifacts = (artifacts != null)
				? Collections.unmodifiableList(new ArrayList<>(artifacts))
				: Collections.emptyList();
	}

	public String getId() {
		return this.id;
	}

	public List<BuildArtifact> getArtifacts() {
		return this.artifacts;
	}

}
