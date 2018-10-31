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

package io.spring.concourse.artifactoryresource.command.payload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InRequest}.
 *
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
@JsonTest
public class InRequestTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Source source = new Source("http://localhost:8181", "username", "password",
			"my-build");

	private Version version = new Version("1234");

	private InRequest.Params params = new InRequest.Params(false, false, false, false);

	@Autowired
	private JacksonTester<InRequest> json;

	@Test
	public void createWhenSourceIsNullShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Source must not be null");
		new InRequest(null, this.version, this.params);
	}

	@Test
	public void createWhenVersionIsNullShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Version must not be null");
		new InRequest(this.source, null, this.params);
	}

	@Test
	public void createWhenParamsIsNullShouldUseDefauls() throws Exception {
		InRequest request = new InRequest(this.source, this.version, null);
		assertThat(request.getParams().isGenerateMavenMetadata()).isTrue();
		assertThat(request.getParams().isSaveBuildInfo()).isFalse();
	}

	@Test
	public void readShouldDeserialize() throws Exception {
		InRequest request = this.json.readObject("in-request.json");
		assertThat(request.getSource().getUri()).isEqualTo("http://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getVersion().getBuildNumber()).isEqualTo("5678");
		assertThat(request.getParams().isGenerateMavenMetadata()).isFalse();
		assertThat(request.getParams().isSaveBuildInfo()).isTrue();
	}

	@Test
	public void readWhenMissingGenerateMavenMetadataShouldDeserialize() throws Exception {
		InRequest request = this.json
				.readObject("in-request-without-generate-maven-metadata.json");
		assertThat(request.getParams().isGenerateMavenMetadata()).isTrue();
	}

}
