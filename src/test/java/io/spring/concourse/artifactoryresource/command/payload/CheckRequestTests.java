/*
 * Copyright 2017 the original author or authors.
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
 * Tests for {@link CheckRequest}.
 *
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
@JsonTest
public class CheckRequestTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private JacksonTester<CheckRequest> json;

	@Test
	public void createWhenSourceIsNullShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Source must not be null");
		new CheckRequest(null, new Version("1234"));
	}

	@Test
	public void readWhenMissingVersionShouldDeserialize() throws Exception {
		CheckRequest request = this.json.readObject("check-request.json");
		assertThat(request.getSource().getUri()).isEqualTo("http://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getVersion()).isNull();
	}

	@Test
	public void readWhenHasVersionShouldDeserialize() throws Exception {
		CheckRequest request = this.json.readObject("check-request-with-version.json");
		assertThat(request.getSource().getUri()).isEqualTo("http://repo.example.com");
		assertThat(request.getSource().getUsername()).isEqualTo("admin");
		assertThat(request.getSource().getPassword()).isEqualTo("password");
		assertThat(request.getVersion().getBuildNumber()).isEqualTo("1234");
	}

}
