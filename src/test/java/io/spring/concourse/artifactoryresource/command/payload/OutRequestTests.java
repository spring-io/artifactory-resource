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

import java.util.List;

import io.spring.concourse.artifactoryresource.command.payload.OutRequest.ArtifactSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OutRequest}.
 *
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
@JsonTest
public class OutRequestTests {

	private Source source = new Source("http://localhost:8181", "username", "password",
			"my-build");

	private OutRequest.Params params = new OutRequest.Params(false, "libs-snapshot-local",
			"1234", "folder", null, null, null, null, null, null, null);

	@Autowired
	private JacksonTester<OutRequest> json;

	@Test
	public void createWhenSourceIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new OutRequest(null, this.params))
				.withMessage("Source must not be null");
	}

	@Test
	public void createWhenParamsIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new OutRequest(this.source, null))
				.withMessage("Params must not be null");
	}

	@Test
	public void createParamsWhenFolderIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new OutRequest.Params(false, "libs-snapshot-local",
						"1234", "", null, null, null, null, null, null, null))
				.withMessage("Folder must not be empty");
	}

	@Test
	public void createParamsWhenRepoIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new OutRequest.Params(false, "", "1234", "folder", null,
						null, null, null, null, null, null))
				.withMessage("Repo must not be empty");
	}

	@Test
	public void readDeserializesJson() throws Exception {
		OutRequest request = this.json.readObject("out-request.json");
		assertThat(request.getSource().getUri()).isEqualTo("https://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getParams().getBuildNumber()).isEqualTo("1234");
		assertThat(request.getParams().getRepo()).isEqualTo("libs-snapshot-local");
		assertThat(request.getParams().getFolder()).isEqualTo("dist");
		assertThat(request.getParams().getInclude()).containsExactly("**");
		assertThat(request.getParams().getExclude()).containsExactly("foo", "bar");
		assertThat(request.getParams().getModuleLayout()).isEqualTo("maven");
		assertThat(request.getParams().getBuildUri()).isEqualTo("https://ci.example.com");
		assertThat(request.getParams().isStripSnapshotTimestamps()).isEqualTo(false);
		assertThat(request.getParams().isDisableChecksumUploads()).isEqualTo(true);
		List<ArtifactSet> artifactSet = request.getParams().getArtifactSet();
		assertThat(artifactSet).hasSize(1);
		assertThat(artifactSet.get(0).getInclude()).containsExactly("**/*.zip");
		assertThat(artifactSet.get(0).getExclude()).containsExactly("**/foo.zip");
		assertThat(artifactSet.get(0).getProperties()).hasSize(2)
				.containsEntry("zip-type", "docs").containsEntry("zip-deployed", "false");
	}

	@Test
	public void readWhenHasNoArtifactSetPropertiesUsesEmptyCollection() throws Exception {
		OutRequest request = this.json
				.readObject("out-request-without-artifact-set-properties.json");
		assertThat(request.getParams().getArtifactSet().get(0).getProperties()).isEmpty();
	}

}
