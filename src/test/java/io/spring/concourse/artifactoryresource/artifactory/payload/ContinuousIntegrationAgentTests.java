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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link ContinuousIntegrationAgent}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@JsonTest
public class ContinuousIntegrationAgentTests {

	@Autowired
	private JacksonTester<ContinuousIntegrationAgent> json;

	@Test
	public void createWhenNameIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new ContinuousIntegrationAgent("", null))
			.withMessage("Name must not be empty");
	}

	@Test
	public void writeSerializesJson() throws Exception {
		ContinuousIntegrationAgent agent = new ContinuousIntegrationAgent("Concourse", "3.0.0");
		assertThat(this.json.write(agent)).isEqualToJson("continuous-integration-agent.json");
	}

}
