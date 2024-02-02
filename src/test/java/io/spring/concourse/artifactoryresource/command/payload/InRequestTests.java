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

package io.spring.concourse.artifactoryresource.command.payload;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link InRequest}.
 *
 * @author Phillip Webb
 * @author Gabriel Petrovay
 */
@JsonTest
class InRequestTests {

	private Source source = new Source("http://localhost:8181", "username", "password", "my-build", "my-project");

	private Version version = new Version("1234", ArtifactoryDateFormat.parse("2014-01-20T12:01:02.003Z"));

	private InRequest.Params params = new InRequest.Params(false, false, false, false, false, null);

	@Autowired
	private JacksonTester<InRequest> json;

	@Test
	void createWhenSourceIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new InRequest(null, this.version, this.params))
			.withMessage("Source must not be null");
	}

	@Test
	void createWhenVersionIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new InRequest(this.source, null, this.params))
			.withMessage("Version must not be null");
	}

	@Test
	void createWhenParamsIsNullUsesDefauls() {
		InRequest request = new InRequest(this.source, this.version, null);
		assertThat(request.getParams().isGenerateMavenMetadata()).isTrue();
		assertThat(request.getParams().isSaveBuildInfo()).isFalse();
		assertThat(request.getParams().isDownloadArtifacts()).isTrue();
		assertThat(request.getParams().isDownloadChecksums()).isTrue();
		assertThat(request.getParams().getThreads()).isEqualTo(1);
	}

	@Test
	void readDeserializesJson() throws Exception {
		InRequest request = this.json.readObject("in-request.json");
		assertThat(request.getSource().getUri()).isEqualTo("https://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getVersion().getBuildNumber()).isEqualTo("5678");
		assertThat(request.getParams().isGenerateMavenMetadata()).isFalse();
		assertThat(request.getParams().isSaveBuildInfo()).isTrue();
		assertThat(request.getParams().isDownloadArtifacts()).isFalse();
		assertThat(request.getParams().isDownloadChecksums()).isFalse();
		assertThat(request.getParams().getThreads()).isEqualTo(8);
	}

	@Test
	void readDeserializesJsonWithProxy() throws Exception {
		InRequest request = this.json.readObject("in-request-with-proxy.json");
		assertThat(request.getSource().getUri()).isEqualTo("https://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getSource().getProxy())
			.isEqualTo(new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)));
		assertThat(request.getVersion().getBuildNumber()).isEqualTo("5678");
	}

	@Test
	void readWhenMissingGenerateMavenMetadataDeserializesJson() throws Exception {
		InRequest request = this.json.readObject("in-request-without-generate-maven-metadata.json");
		assertThat(request.getParams().isGenerateMavenMetadata()).isTrue();
	}

}
