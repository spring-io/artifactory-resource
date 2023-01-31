/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.artifactory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link BuildNumber}.
 *
 * @author Phillip Webb
 */
class BuildNumberTests {

	@Test
	void ofWhenNumberIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> BuildNumber.of(null))
				.withMessage("Build number must not be empty");
	}

	@Test
	void ofWhenNumberIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> BuildNumber.of(""))
				.withMessage("Build number must not be empty");
	}

	@Test
	void ofCreatesBuildNumber() {
		assertThat(BuildNumber.of("123")).hasToString("123");
	}

	@Test
	void ofWithPrefixCreatesBuildNumber() {
		assertThat(BuildNumber.of("123", "456")).hasToString("123456");
	}

	@Test
	void ofWithPrefixWhenPrefixIsNullCreatesBuildNumber() {
		assertThat(BuildNumber.of(null, "456")).hasToString("456");

	}

	@Test
	void hashCodeAndEquals() {
		BuildNumber n1 = BuildNumber.of("123");
		BuildNumber n2 = BuildNumber.of("12", "3");
		BuildNumber n3 = BuildNumber.of("124");
		assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
		assertThat(n1).isEqualTo(n1).isEqualTo(n2).isNotEqualTo(n3);
	}

	@Test
	void toStringReturnsString() {
		assertThat(BuildNumber.of("test")).hasToString("test");
	}

}
