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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link InResponse}.
 *
 * @author Phillip Webb
 */
@JsonTest
class InResponseTests {

	@Autowired
	private JacksonTester<InResponse> json;

	@Test
	void createWhenVersionIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException().isThrownBy(() -> new InResponse(null, null))
				.withMessage("Version must not be null");
	}

	@Test
	void writeSerializesJson() throws Exception {
		List<Metadata> metadata = new ArrayList<>();
		metadata.add(new Metadata("foo", "bar"));
		metadata.add(new Metadata("bin", "bag"));
		InResponse response = new InResponse(new Version("1234"), metadata);
		assertThat(this.json.write(response)).isEqualToJson("in-response.json");
	}

	@Test
	void writeWhenMetadataIsNullSerializesJson() throws Exception {
		InResponse response = new InResponse(new Version("5678"), null);
		assertThat(this.json.write(response)).isEqualToJson("in-response-without-metadata.json");
	}

}
