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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DeployedArtifactQueryResponse}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@JsonTest
public class DeployedArtifactQueryResponseTests {

	@Autowired
	private JacksonTester<DeployedArtifactQueryResponse> json;

	@Test
	public void readDeserializesJson() throws Exception {
		DeployedArtifactQueryResponse response = this.json.readObject("deployed-artifacts.json");
		assertThat(response.getResults()).hasSize(1);
		DeployedArtifact artifact = response.getResults().get(0);
		assertThat(artifact.getRepo()).isEqualTo("libs-release-local");
		assertThat(artifact.getPath()).isEqualTo("org/jfrog/artifactory");
		assertThat(artifact.getName()).isEqualTo("artifactory.war");
		assertThat(artifact.getType()).isEqualTo("item type");
		assertThat(artifact.getSize()).isEqualTo(75500000);
		assertThat(artifact.getCreated()).isEqualTo(ArtifactoryDateFormat.parse("2017-06-19T17:17:33.423-0700"));
		assertThat(artifact.getCreatedBy()).isEqualTo("jfrog");
		assertThat(artifact.getModified()).isEqualTo(ArtifactoryDateFormat.parse("2017-06-19T17:17:34.423-0700"));
		assertThat(artifact.getModifiedBy()).isEqualTo("spring");
		assertThat(artifact.getUpdated()).isEqualTo(ArtifactoryDateFormat.parse("2017-06-19T17:17:35.423-0700"));
		assertThat(response.getRange().getStartPos()).isEqualTo(0);
		assertThat(response.getRange().getEndPos()).isEqualTo(1);
		assertThat(response.getRange().getTotal()).isEqualTo(1);
	}

}
