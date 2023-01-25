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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single response from request for build runs.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class BuildRunsResponse {

	private final String uri;

	private final List<BuildRun> buildsRuns;

	@JsonCreator
	public BuildRunsResponse(@JsonProperty("uri") String uri,
			@JsonProperty("buildsNumbers") List<BuildRun> buildsRuns) {
		this.uri = uri;
		this.buildsRuns = (buildsRuns != null) ? Collections.unmodifiableList(new ArrayList<>(buildsRuns))
				: Collections.emptyList();
	}

	public String getUri() {
		return this.uri;
	}

	public List<BuildRun> getBuildsRuns() {
		return this.buildsRuns;
	}

}
