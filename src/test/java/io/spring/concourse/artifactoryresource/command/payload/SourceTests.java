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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link Source}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@RunWith(SpringRunner.class)
@JsonTest
public class SourceTests {

	@Autowired
	private JacksonTester<Source> json;

	@Test
	public void createWhenUriIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException().isThrownBy(() -> new Source("", "username", "password", "my-build"))
				.withMessage("URI must not be empty");
	}

	@Test
	public void createWhenUsernameIsEmptyDoesNotThrowException() throws Exception {
		new Source("https://repo.example.com", "", "password", "my-build");
	}

	@Test
	public void createWhenPasswordIsEmptyDoesNotThrowException() throws Exception {
		new Source("https://repo.example.com", "username", "", "my-build");
	}

	@Test
	public void createWhenBuildNameIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Source("https://repo.example.com", "username", "password", ""))
				.withMessage("Build Name must not be empty");
	}

	@Test
	public void readDeserializesJson() throws Exception {
		Source source = this.json.readObject("source.json");
		assertThat(source.getUri()).isEqualTo("https://repo.example.com");
		assertThat(source.getUsername()).isEqualTo("admin");
		assertThat(source.getPassword()).isEqualTo("password");
		assertThat(source.getBuildName()).isEqualTo("my-build");
	}

}
