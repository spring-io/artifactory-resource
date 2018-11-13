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

import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildRunsResponse}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
@JsonTest
public class BuildRunsResponseTests {

	@Autowired
	private JacksonTester<BuildRunsResponse> json;

	@Test
	public void readDeserializesJson() throws Exception {
		BuildRunsResponse response = this.json.readObject("build-runs-response.json");
		assertThat(response.getUri())
				.isEqualTo("http://localhost:8081/artifactory/api/build/my-build");
		assertThat(response.getBuildsRuns()).hasSize(2);
		assertThat(response.getBuildsRuns().get(0).getBuildNumber()).isEqualTo("1234");
		assertThat(response.getBuildsRuns().get(0).getUri()).isEqualTo("/1234");
		assertThat(response.getBuildsRuns().get(0).getStarted())
				.isEqualTo(ArtifactoryDateFormat.parse("2014-09-28T12:00:19.893+0000"));
		assertThat(response.getBuildsRuns().get(1).getBuildNumber()).isEqualTo("5678");
		assertThat(response.getBuildsRuns().get(1).getUri()).isEqualTo("/5678");
		assertThat(response.getBuildsRuns().get(1).getStarted())
				.isEqualTo(ArtifactoryDateFormat.parse("2014-09-30T12:00:19.893+0000"));
	}

}
