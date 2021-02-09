/*
 * Copyright 2017-2021 the original author or authors.
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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.List;

import io.spring.concourse.artifactoryresource.command.payload.OutRequest.ArtifactSet;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OutRequest}.
 *
 * @author Phillip Webb
 * @author Gabriel Petrovay
 */
@JsonTest
class OutRequestTests {

	private Source source = new Source("http://localhost:8181", "username", "password", "my-build", null, null);

	private OutRequest.Params params = new OutRequest.Params(false, "libs-snapshot-local", "1234", "folder", null, null,
			null, null, null, null, null, null, null, null);

	@Autowired
	private JacksonTester<OutRequest> json;

	@Test
	void createWhenSourceIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest(null, this.params))
				.withMessage("Source must not be null");
	}

	@Test
	void createWhenParamsIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest(this.source, null))
				.withMessage("Params must not be null");
	}

	@Test
	void createParamsWhenFolderIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest.Params(false, "libs-snapshot-local",
				"1234", "", null, null, null, null, null, null, null, null, null, null))
				.withMessage("Folder must not be empty");
	}

	@Test
	void createParamsWhenRepoIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new OutRequest.Params(false, "", "1234", "folder", null,
				null, null, null, null, null, null, null, null, null)).withMessage("Repo must not be empty");
	}

	@Test
	void readDeserializesJson() throws Exception {
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
		assertThat(request.getParams().getThreads()).isEqualTo(8);
		assertThat(request.getParams().getSigningKey()).isNull();
		assertThat(request.getParams().getSigningPassphrase()).isNull();
		List<ArtifactSet> artifactSet = request.getParams().getArtifactSet();
		assertThat(artifactSet).hasSize(1);
		assertThat(artifactSet.get(0).getInclude()).containsExactly("**/*.zip");
		assertThat(artifactSet.get(0).getExclude()).containsExactly("**/foo.zip");
		assertThat(artifactSet.get(0).getProperties()).hasSize(2).containsEntry("zip-type", "docs")
				.containsEntry("zip-deployed", "false");
	}

	@Test
	void readDeserializesJsonWithSensibleStripTimestampDefaults() throws Exception {
		OutRequest requestNone = this.json.readObject("out-request-strip-timestamps-with-module-layout-none.json");
		assertThat(requestNone.getParams().getModuleLayout()).isEqualTo("none");
		assertThat(requestNone.getParams().isStripSnapshotTimestamps()).isEqualTo(false);
		OutRequest requestMaven = this.json.readObject("out-request-strip-timestamps-with-module-layout-maven.json");
		assertThat(requestMaven.getParams().getModuleLayout()).isEqualTo("maven");
		assertThat(requestMaven.getParams().isStripSnapshotTimestamps()).isEqualTo(true);
	}

	@Test
	void readDeserializesJsonWithProxy() throws Exception {
		OutRequest request = this.json.readObject("out-request-with-proxy.json");
		assertThat(request.getSource().getUri()).isEqualTo("https://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getSource().getProxy())
				.isEqualTo(new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)));
		assertThat(request.getParams().getBuildNumber()).isEqualTo("1234");
		assertThat(request.getParams().getRepo()).isEqualTo("libs-snapshot-local");
		assertThat(request.getParams().getFolder()).isEqualTo("dist");
		assertThat(request.getParams().getBuildUri()).isEqualTo("https://ci.example.com");
	}

	@Test
	void readDeserializesJsonWithSigning() throws Exception {
		OutRequest request = this.json.readObject("out-request-with-signing.json");
		assertThat(request.getParams().getSigningKey()).isEqualTo("sign.txt");
		assertThat(request.getParams().getSigningPassphrase()).isEqualTo("secret");
	}

	@Test
	void readWhenHasNoArtifactSetPropertiesUsesEmptyCollection() throws Exception {
		OutRequest request = this.json.readObject("out-request-without-artifact-set-properties.json");
		assertThat(request.getParams().getArtifactSet().get(0).getProperties()).isEmpty();
	}

}
