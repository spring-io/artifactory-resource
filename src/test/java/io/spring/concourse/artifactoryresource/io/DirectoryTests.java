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

package io.spring.concourse.artifactoryresource.io;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
public class DirectoryTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void createWhenPathIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Directory((String) null))
				.withMessage("File must not be null");
	}

	@Test
	public void createWhenFileIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException().isThrownBy(() -> new Directory((File) null))
				.withMessage("File must not be null");
	}

	@Test
	public void createWhenFileDoesNotExistThrowsException() throws Exception {
		File file = this.temporaryFolder.newFile();
		file.delete();
		assertThatIllegalStateException().isThrownBy(() -> new Directory(file))
				.withMessageContaining("does not exist");
	}

	@Test
	public void createWhenFileIsNotDirectoryThrowsException() throws Exception {
		File file = this.temporaryFolder.newFile();
		assertThatIllegalStateException().isThrownBy(() -> new Directory(file))
				.withMessageContaining("is not a directory");
	}

	@Test
	public void getFileReturnsFile() throws Exception {
		File file = this.temporaryFolder.newFolder();
		Directory directory = new Directory(file);
		assertThat(directory.getFile()).isEqualTo(file);
	}

	@Test
	public void toStringReturnsFilePath() throws Exception {
		File file = this.temporaryFolder.newFolder();
		Directory directory = new Directory(file);
		String expected = StringUtils.cleanPath(file.getPath());
		assertThat(directory.toString()).isEqualTo(expected);
	}

	@Test
	public void getSubDirectoryWhenPathIsNullReturnsThis() throws Exception {
		File file = this.temporaryFolder.newFolder();
		Directory directory = new Directory(file);
		Directory subdirectory = directory.getSubDirectory(null);
		assertThat(directory).isEqualTo(subdirectory);
	}

	@Test
	public void getSubDirectoryWhenPathIsEmptyReturnsThis() throws Exception {
		File file = this.temporaryFolder.newFolder();
		Directory directory = new Directory(file);
		Directory subdirectory = directory.getSubDirectory("");
		assertThat(directory).isEqualTo(subdirectory);
	}

	@Test
	public void getSubDirectoryGetsSubDirectory() throws Exception {
		File file = this.temporaryFolder.newFolder();
		Directory directory = new Directory(file);
		File nested = new File(file, "nested");
		nested.mkdirs();
		Directory subdirectory = directory.getSubDirectory("nested");
		assertThat(subdirectory.getFile()).isEqualTo(nested);
	}

	@Test
	public void createFromArgsUsesArgument() throws Exception {
		File file = this.temporaryFolder.newFolder();
		String path = StringUtils.cleanPath(file.getPath());
		ApplicationArguments args = new DefaultApplicationArguments(
				new String[] { "in", path });
		Directory directory = Directory.fromArgs(args);
		assertThat(directory.getFile()).isEqualTo(file);
	}

}
