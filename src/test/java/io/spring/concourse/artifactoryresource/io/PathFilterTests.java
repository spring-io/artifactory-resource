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

package io.spring.concourse.artifactoryresource.io;

import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link PathFilter}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class PathFilterTests {

	@Test
	public void createWhenIncludeIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new PathFilter(null, Collections.emptyList()))
				.withMessage("Include must not be null");
	}

	@Test
	public void createWhenExcludeIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new PathFilter(Collections.emptyList(), null))
				.withMessage("Exclude must not be null");
	}

	@Test
	public void isMatchWhenIncludeIsEmptyAndExcludeIsEmptyReturnsTrue() throws Exception {
		PathFilter filter = new PathFilter(Collections.emptyList(),
				Collections.emptyList());
		assertThat(filter.isMatch("/foo")).isTrue();
	}

	@Test
	public void isMatchWhenIncludeIsEmptyAndExcludeMatchesReturnsFalse()
			throws Exception {
		PathFilter filter = new PathFilter(Collections.emptyList(),
				Collections.singletonList("/**/foo"));
		assertThat(filter.isMatch("/foo/bar")).isTrue();
		assertThat(filter.isMatch("/bar/foo")).isFalse();
	}

	@Test
	public void isMatchWhenIncludeMatchesAndExcludeIsEmptyReturnsTrue() throws Exception {
		PathFilter filter = new PathFilter(Collections.singletonList("/**/foo"),
				Collections.emptyList());
		assertThat(filter.isMatch("/foo/bar")).isFalse();
		assertThat(filter.isMatch("/bar/foo")).isTrue();
	}

	@Test
	public void isMatchWhenIncludeMatchesExtensionAndExcludeIsEmptyReturnsTrue()
			throws Exception {
		PathFilter filter = new PathFilter(
				Collections.singletonList("/**/spring-boot-docs-*.zip"),
				Collections.emptyList());
		assertThat(filter.isMatch("/org/springframework/boot/spring-boot-docs/"
				+ "2.0.0.BUILD-SNAPSHOT/spring-boot-docs-2.0.0.BUILD-20170920.065551-1.zip"))
						.isTrue();
	}

	@Test
	public void isMatchWhenIncludeMatchesAndExcludeMatchesReturnsFalse()
			throws Exception {
		PathFilter filter = new PathFilter(Collections.singletonList("/foo/**"),
				Collections.singletonList("/**/bar"));
		assertThat(filter.isMatch("/foo/bar")).isFalse();
		assertThat(filter.isMatch("/foo/baz")).isTrue();
	}

}
