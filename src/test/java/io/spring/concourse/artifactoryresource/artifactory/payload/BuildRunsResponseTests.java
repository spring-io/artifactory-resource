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

import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildRunsResponse}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
public class BuildRunsResponseTests {

	@Autowired
	private JacksonTester<BuildRunsResponse> json;

	@Test
	public void readDeserializesJson() throws Exception {
		BuildRunsResponse response = this.json.readObject("build-runs-response.json");
		assertThat(response.getResults()).hasSize(2);
		assertThat(response.getResults().get(0).getBuildNumber()).isEqualTo("1234");
		assertThat(response.getResults().get(0).getStarted())
				.isEqualTo(ArtifactoryDateFormat.parse("2014-09-28T12:00:19.893Z"));
		assertThat(response.getResults().get(1).getBuildNumber()).isEqualTo("5678");
		assertThat(response.getResults().get(1).getStarted())
				.isEqualTo(ArtifactoryDateFormat.parse("2014-09-30T12:00:19.893Z"));
	}

}
