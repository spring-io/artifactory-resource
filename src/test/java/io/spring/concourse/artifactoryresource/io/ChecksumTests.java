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

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.entry;

/**
 * Tests for {@link Checksum}.
 *
 * @author Phillip Webb
 */
public class ChecksumTests {

	private static final String SOURCE = "foo";

	private static final String SHA1 = "0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33";

	private static final String MD5 = "acbd18db4cc2f85cedef654fccc4a4d8";

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void getFileExtensionReturnsExtension() {
		assertThat(Checksum.MD5.getFileExtension()).isEqualTo(".md5");
		assertThat(Checksum.SHA1.getFileExtension()).isEqualTo(".sha1");
	}

	@Test
	public void validateWhenNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> Checksum.SHA1.validate(null))
				.withMessage("SHA1 must not be empty");
	}

	@Test
	public void validateWhenEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> Checksum.MD5.validate(""))
				.withMessage("MD5 must not be empty");
	}

	@Test
	public void validateWhenTooLongThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> Checksum.SHA1.validate("a9993e364706816aba3e25717850c26c9cd0d89d1"))
				.withMessage("SHA1 must be 40 characters long");
	}

	@Test
	public void validateWhenTooShortThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> Checksum.SHA1.validate("a9993e364706816aba3e25717850c26c9cd0d89"))
				.withMessage("SHA1 must be 40 characters long");
	}

	@Test
	public void generateChecksumFilesGeneratesChecksumFiles() throws Exception {
		File file = this.temp.newFile();
		FileCopyUtils.copy(SOURCE, new FileWriter(file));
		Checksum.generateChecksumFiles(file);
		assertThat(new File(file.getParentFile(), file.getName() + ".sha1"))
				.hasContent(SHA1);
		assertThat(new File(file.getParentFile(), file.getName() + ".md5"))
				.hasContent(MD5);
	}

	@Test
	public void generateChecksumFileWhenChecksumFileDoesNotGenerate() throws Exception {
		File file = this.temp.newFile("test.md5");
		FileCopyUtils.copy(SOURCE, new FileWriter(file));
		Checksum.generateChecksumFiles(file);
		assertThat(new File(file.getParentFile(), file.getName() + ".sha1"))
				.doesNotExist();
		assertThat(new File(file.getParentFile(), file.getName() + ".md5"))
				.doesNotExist();
	}

	@Test
	public void isChecksumFileWhenChecksumFileReturnsTrue() {
		assertThat(Checksum.isChecksumFile("foo.md5")).isTrue();
		assertThat(Checksum.isChecksumFile("foo/bar.MD5")).isTrue();
		assertThat(Checksum.isChecksumFile("foo.sha1")).isTrue();
	}

	@Test
	public void isChecksumFileWhenNotChecksumFileReturnsFalse() {
		assertThat(Checksum.isChecksumFile("foo.xml")).isFalse();
	}

	@Test
	public void getFileExtensionsReturnsExtensions() {
		assertThat(Checksum.getFileExtensions()).containsOnly(".md5", ".sha1");
	}

	@Test
	public void calculateAllFromResourceReturnsChecksums() {
		ByteArrayResource resource = new ByteArrayResource(SOURCE.getBytes());
		Map<Checksum, String> checksums = Checksum.calculateAll(resource);
		assertThat(checksums).containsOnly(entry(Checksum.MD5, MD5),
				entry(Checksum.SHA1, SHA1));
	}

	@Test
	public void calculateAllFromStringReturnsChecksums() {
		Map<Checksum, String> checksums = Checksum.calculateAll(SOURCE);
		assertThat(checksums).containsOnly(entry(Checksum.MD5, MD5),
				entry(Checksum.SHA1, SHA1));
	}

}
