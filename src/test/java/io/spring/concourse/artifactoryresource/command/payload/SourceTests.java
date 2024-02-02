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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link Source}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Gabriel Petrovay
 */
@JsonTest
class SourceTests {

	@Autowired
	private JacksonTester<Source> json;

	@Test
	void createWhenUriIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Source("", "username", "password", "my-build", null))
			.withMessage("URI must not be empty");
	}

	@Test
	void createWhenUsernameIsEmptyDoesNotThrowException() {
		new Source("https://repo.example.com", "", "password", "my-build", null);
	}

	@Test
	void createWhenPasswordIsEmptyDoesNotThrowException() {
		new Source("https://repo.example.com", "username", "", "my-build", null);
	}

	@Test
	void createWhenBuildNameIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> new Source("https://repo.example.com", "username", "password", "", null, null, null, null, null))
			.withMessage("Build Name must not be empty");
	}

	@Test
	void createWhenHasProxyHostWithoutProxyPortThrowsException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new Source("https://repo.example.com", "username", "password", "my-build", null, null,
					null, "proxy.example.com", null))
			.withMessage("Proxy port must be provided");
	}

	@Test
	void readDeserializesJson() throws Exception {
		Source source = this.json.readObject("source.json");
		assertThat(source.getUri()).isEqualTo("https://repo.example.com");
		assertThat(source.getUsername()).isEqualTo("admin");
		assertThat(source.getPassword()).isEqualTo("password");
		assertThat(source.getBuildName()).isEqualTo("my-build");
		assertThat(source.getBuildNumberPrefix()).isEqualTo("main-");
		assertThat(source.getCheckLimit()).isEqualTo(1);
	}

	@Test
	void readDeserializesJsonWithProxy() throws Exception {
		Source source = this.json.readObject("source-with-proxy.json");
		assertThat(source.getUri()).isEqualTo("https://repo.example.com");
		assertThat(source.getUsername()).isEqualTo("admin");
		assertThat(source.getPassword()).isEqualTo("password");
		assertThat(source.getBuildName()).isEqualTo("my-build");
		assertThat(source.getProxy()).isEqualTo(new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)));
	}

}
