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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link Checksums}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ChecksumsTests {

	private static final String SHA1 = "a9993e364706816aba3e25717850c26c9cd0d89d";

	private static final String MD5 = "900150983cd24fb0d6963f7d28e17f72";

	@Test
	public void createWhenSha1IsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Checksums("", MD5))
				.withMessage("SHA1 must not be empty");
	}

	@Test
	public void createWhenMd5IsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Checksums(SHA1, ""))
				.withMessage("MD5 must not be empty");
	}

	@Test
	public void createWhenSha1IsIncorrectLengthThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Checksums("0", MD5))
				.withMessage("SHA1 must be 40 characters long");
	}

	@Test
	public void createWhenMd5IsIncorrectLengthThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Checksums(SHA1, "0"))
				.withMessage("MD5 must be 32 characters long");
	}

	@Test
	public void getSha1GetsSha1() {
		Checksums checksums = new Checksums(SHA1, MD5);
		assertThat(checksums.getSha1()).isEqualTo(SHA1);
	}

	@Test
	public void getMd5GetsMd5() {
		Checksums checksums = new Checksums(SHA1, MD5);
		assertThat(checksums.getMd5()).isEqualTo(MD5);
	}

}
