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
 * Tests for {@link CheckResponse}.
 *
 * @author Phillip Webb
 */
@JsonTest
class CheckResponseTests {

	@Autowired
	private JacksonTester<CheckResponse> json;

	@Test
	void createWhenVersionsIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new CheckResponse(null))
			.withMessage("Versions must not be null");
	}

	@Test
	void writeSerializesJson() throws Exception {
		List<Version> versions = new ArrayList<>();
		versions.add(new Version("1234", ArtifactoryDateFormat.parse("2014-01-20T12:01:02.003Z")));
		versions.add(new Version("5678", ArtifactoryDateFormat.parse("2014-01-21T12:01:02.003Z")));
		CheckResponse value = new CheckResponse(versions);
		assertThat(this.json.write(value)).isEqualToJson("check-response.json");
	}

}
