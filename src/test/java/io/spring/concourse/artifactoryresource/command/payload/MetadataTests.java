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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link Metadata}.
 *
 * @author Phillip Webb
 */
@JsonTest
class MetadataTests {

	@Autowired
	private JacksonTester<Metadata> json;

	@Test
	void createWhenMisstingNameThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Metadata("", "value"))
			.withMessage("Name must not be empty");
	}

	@Test
	void writeSerializesJson() throws Exception {
		assertThat(this.json.write(new Metadata("foo", "bar"))).isEqualToJson("metadata.json");
	}

}
