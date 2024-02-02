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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link OutResponse}.
 *
 * @author Phillip Webb
 */
@JsonTest
class OutResponseTests {

	private static final String BUILD_NUMBER = "1234";

	private static final Instant BUILD_TIMESTAMP = ArtifactoryDateFormat.parse("2014-01-20T12:01:02.003Z");

	@Autowired
	private JacksonTester<OutResponse> json;

	@Test
	void createWhenVersionIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new InResponse(null, null))
			.withMessage("Version must not be null");
	}

	@Test
	void writeSerializesJson() throws Exception {
		List<Metadata> metadata = new ArrayList<>();
		metadata.add(new Metadata("foo", "bar"));
		metadata.add(new Metadata("bin", "bag"));
		OutResponse response = new OutResponse(new Version(BUILD_NUMBER, BUILD_TIMESTAMP), metadata);
		assertThat(this.json.write(response)).isEqualToJson("out-response.json");
	}

	@Test
	void writeWhenMetadataIsNullSerializesJson() throws Exception {
		OutResponse response = new OutResponse(new Version(BUILD_NUMBER, BUILD_TIMESTAMP), null);
		assertThat(this.json.write(response)).isEqualToJson("out-response-without-metadata.json");
	}

}
