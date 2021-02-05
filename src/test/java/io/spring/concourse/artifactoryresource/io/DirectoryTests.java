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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link Directory}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class DirectoryTests {

	@TempDir
	File tempDir;

	@Test
	void createWhenPathIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Directory((String) null))
				.withMessage("File must not be null");
	}

	@Test
	void createWhenFileIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Directory((File) null))
				.withMessage("File must not be null");
	}

	@Test
	void createWhenFileDoesNotExistThrowsException() {
		this.tempDir.delete();
		assertThatIllegalStateException().isThrownBy(() -> new Directory(this.tempDir))
				.withMessageContaining("does not exist");
	}

	@Test
	void createWhenFileIsNotDirectoryThrowsException() throws Exception {
		File file = File.createTempFile("test", "", this.tempDir);
		assertThatIllegalStateException().isThrownBy(() -> new Directory(file))
				.withMessageContaining("is not a directory");
	}

	@Test
	void isEmptyWhenEmptyReturnsTrue() {
		Directory directory = new Directory(this.tempDir);
		assertThat(directory.isEmpty()).isTrue();
	}

	@Test
	void isEmptyWhenNotEmptyReturnsFalse() {
		new File(this.tempDir, "nested").mkdirs();
		Directory directory = new Directory(this.tempDir);
		assertThat(directory.isEmpty()).isFalse();
	}

	@Test
	void getFileReturnsFile() {
		File file = this.tempDir;
		Directory directory = new Directory(file);
		assertThat(directory.getFile()).isEqualTo(file);
	}

	@Test
	void toStringReturnsFilePath() {
		File file = this.tempDir;
		Directory directory = new Directory(file);
		String expected = StringUtils.cleanPath(file.getPath());
		assertThat(directory.toString()).isEqualTo(expected);
	}

	@Test
	void getSubDirectoryWhenPathIsNullReturnsThis() {
		File file = this.tempDir;
		Directory directory = new Directory(file);
		Directory subdirectory = directory.getSubDirectory(null);
		assertThat(directory).isEqualTo(subdirectory);
	}

	@Test
	void getSubDirectoryWhenPathIsEmptyReturnsThis() {
		File file = this.tempDir;
		Directory directory = new Directory(file);
		Directory subdirectory = directory.getSubDirectory("");
		assertThat(directory).isEqualTo(subdirectory);
	}

	@Test
	void getSubDirectoryGetsSubDirectory() {
		File file = this.tempDir;
		Directory directory = new Directory(file);
		File nested = new File(file, "nested");
		nested.mkdirs();
		Directory subdirectory = directory.getSubDirectory("nested");
		assertThat(subdirectory.getFile()).isEqualTo(nested);
	}

	@Test
	void createFromArgsUsesArgument() {
		File file = this.tempDir;
		String path = StringUtils.cleanPath(file.getPath());
		ApplicationArguments args = new DefaultApplicationArguments(new String[] { "in", path });
		Directory directory = Directory.fromArgs(args);
		assertThat(directory.getFile()).isEqualTo(file);
	}

}
