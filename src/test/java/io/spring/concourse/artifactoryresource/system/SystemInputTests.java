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

package io.spring.concourse.artifactoryresource.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link SystemInput}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class SystemInputTests {

	private MockEnvironment environment;

	@BeforeEach
	void setUp() {
		this.environment = new MockEnvironment();
	}

	@Test
	void readWhenNoDataTimesout() {
		SystemInput input = new SystemInput(this.environment, new MockSystemStreams(""), new ObjectMapper(), 10);
		assertThatIllegalStateException().isThrownBy(() -> input.read(String[].class))
			.withMessage("Timeout waiting for input");
	}

	@Test
	void readDeserializesJson() throws Exception {
		SystemInput input = new SystemInput(this.environment, new MockSystemStreams("[\"foo\",\"bar\"]"),
				new ObjectMapper());
		String[] result = input.read(String[].class);
		assertThat(result).containsExactly("foo", "bar");
	}

	@Test
	void readResolvesPlaceholders() throws Exception {
		this.environment.setProperty("bar", "hello-world");
		SystemInput input = new SystemInput(this.environment, new MockSystemStreams("[\"foo\",\"${bar}\"]"),
				new ObjectMapper());
		String[] result = input.read(String[].class);
		assertThat(result).containsExactly("foo", "hello-world");
	}

}
