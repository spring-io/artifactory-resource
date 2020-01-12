/*
 * Copyright 2017-2020 the original author or authors.
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
import java.io.IOException;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DirectoryScanner}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class DirectoryScannerTests {

	private static final byte[] NO_BYTES = {};

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private DirectoryScanner scanner = new DirectoryScanner();

	@Test
	public void scanWhenNoIncludeExcludeReturnsAllFiles() throws Exception {
		Directory directory = createFiles();
		FileSet files = this.scanner.scan(directory, Collections.emptyList(), Collections.emptyList());
		assertThat(files).extracting((f) -> relativePath(directory, f)).containsExactly("/bar/bar.jar", "/bar/bar.pom",
				"/baz/baz.jar", "/baz/baz.pom");
	}

	@Test
	public void scanWhenUsingIncludesFiltersFiles() throws Exception {
		Directory directory = createFiles();
		FileSet files = this.scanner.scan(directory, Collections.singletonList("**/*.jar"), Collections.emptyList());
		assertThat(files).extracting((f) -> relativePath(directory, f)).containsExactly("/bar/bar.jar", "/baz/baz.jar");
	}

	@Test
	public void scanWhenUsingExcludesFiltersFiles() throws Exception {
		Directory directory = createFiles();
		FileSet files = this.scanner.scan(directory, Collections.emptyList(), Collections.singletonList("**/*.jar"));
		assertThat(files).extracting((f) -> relativePath(directory, f)).containsExactly("/bar/bar.pom", "/baz/baz.pom");
	}

	@Test
	public void scanWhenUsingIncludesAndExcludesFiltersFiles() throws Exception {
		Directory directory = createFiles();
		FileSet files = this.scanner.scan(directory, Collections.singletonList("**/*.jar"),
				Collections.singletonList("**/baz.*"));
		assertThat(files).extracting((f) -> relativePath(directory, f)).containsExactly("/bar/bar.jar");
	}

	@Test
	public void scanWhenUsingSlashPrefixFiltersFiles() throws IOException {
		Directory directory = createFiles();
		FileSet files = this.scanner.scan(directory, Collections.singletonList("/**/*.jar"),
				Collections.singletonList("/**/baz.*"));
		assertThat(files).extracting((f) -> relativePath(directory, f)).containsExactly("/bar/bar.jar");
	}

	private String relativePath(Directory directory, File file) {
		String root = StringUtils.cleanPath(directory.getFile().getPath());
		String path = StringUtils.cleanPath(file.getPath());
		return path.substring(root.length());
	}

	private Directory createFiles() throws IOException {
		File root = this.temporaryFolder.newFolder();
		File barDir = new File(root, "bar");
		touch(new File(barDir, "bar.jar"));
		touch(new File(barDir, "bar.pom"));
		File bazDir = new File(root, "baz");
		touch(new File(bazDir, "baz.jar"));
		touch(new File(bazDir, "baz.pom"));
		return new Directory(root);
	}

	private void touch(File file) throws IOException {
		file.getParentFile().mkdirs();
		FileCopyUtils.copy(NO_BYTES, file);
	}

}
