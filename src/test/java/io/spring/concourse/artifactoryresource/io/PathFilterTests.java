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

package io.spring.concourse.artifactoryresource.io;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link PathFilter}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class PathFilterTests {

	@Test
	void createWhenIncludeIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new PathFilter(null, Collections.emptyList()))
			.withMessage("Include must not be null");
	}

	@Test
	void createWhenExcludeIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new PathFilter(Collections.emptyList(), null))
			.withMessage("Exclude must not be null");
	}

	@Test
	void isMatchWhenIncludeIsEmptyAndExcludeIsEmptyReturnsTrue() {
		PathFilter filter = new PathFilter(Collections.emptyList(), Collections.emptyList());
		assertThat(filter.isMatch("/foo")).isTrue();
	}

	@Test
	void isMatchWhenIncludeIsEmptyAndExcludeMatchesReturnsFalse() {
		PathFilter filter = new PathFilter(Collections.emptyList(), Collections.singletonList("/**/foo"));
		assertThat(filter.isMatch("/foo/bar")).isTrue();
		assertThat(filter.isMatch("/bar/foo")).isFalse();
	}

	@Test
	void isMatchWhenIncludeMatchesAndExcludeIsEmptyReturnsTrue() {
		PathFilter filter = new PathFilter(Collections.singletonList("/**/foo"), Collections.emptyList());
		assertThat(filter.isMatch("/foo/bar")).isFalse();
		assertThat(filter.isMatch("/bar/foo")).isTrue();
	}

	@Test
	void isMatchWhenIncludeMatchesExtensionAndExcludeIsEmptyReturnsTrue() {
		PathFilter filter = new PathFilter(Collections.singletonList("/**/spring-boot-docs-*.zip"),
				Collections.emptyList());
		assertThat(filter.isMatch("/org/springframework/boot/spring-boot-docs/"
				+ "2.0.0.BUILD-SNAPSHOT/spring-boot-docs-2.0.0.BUILD-20170920.065551-1.zip"))
			.isTrue();
	}

	@Test
	void isMatchWhenIncludeMatchesAndExcludeMatchesReturnsFalse() {
		PathFilter filter = new PathFilter(Collections.singletonList("/foo/**"), Collections.singletonList("/**/bar"));
		assertThat(filter.isMatch("/foo/bar")).isFalse();
		assertThat(filter.isMatch("/foo/baz")).isTrue();
	}

}
