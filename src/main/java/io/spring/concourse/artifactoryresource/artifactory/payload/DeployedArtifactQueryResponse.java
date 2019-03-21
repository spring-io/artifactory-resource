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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single response from an Artifactory Query Language request for deployed artifacts.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class DeployedArtifactQueryResponse {

	private List<DeployedArtifact> results;

	private Range range;

	@JsonCreator
	public DeployedArtifactQueryResponse(
			@JsonProperty("results") List<DeployedArtifact> results,
			@JsonProperty("range") Range range) {
		this.results = results;
		this.range = range;
	}

	public List<DeployedArtifact> getResults() {
		return this.results;
	}

	public Range getRange() {
		return this.range;
	}

	/**
	 * The range covered in the {@link DeployedArtifactQueryResponse}.
	 */
	public static class Range {

		private int startPos;

		private int endPos;

		private int total;

		@JsonCreator
		public Range(@JsonProperty("start_pos") int startPos,
				@JsonProperty("end_pos") int endPos, @JsonProperty("total") int total) {
			this.startPos = startPos;
			this.endPos = endPos;
			this.total = total;
		}

		public int getStartPos() {
			return this.startPos;
		}

		public int getEndPos() {
			return this.endPos;
		}

		public int getTotal() {
			return this.total;
		}

	}

}
