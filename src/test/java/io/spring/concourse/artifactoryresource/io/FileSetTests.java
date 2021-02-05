/*
 * Copyright 2017-2021 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.spring.concourse.artifactoryresource.io.FileSet.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link FileSet}.
 *
 * @author Phillip Webb
 */
class FileSetTests {

	@TempDir
	File tempDir;

	@Test
	void ofWhenArrayIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> FileSet.of((File[]) null))
				.withMessage("Files must not be null");
	}

	@Test
	void ofWhenListIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> FileSet.of((List<File>) null))
				.withMessage("Files must not be null");
	}

	@Test
	void ofOrdersOnParentPath() {
		assertThatFileSetIsOrdered("bar/bar.jar", "foo/bar.jar");
	}

	@Test
	void ofWhenIdenticalPathOrdersOnExtension() {
		assertThatFileSetIsOrdered("foo/bar.jar", "foo/bar.war");
	}

	@Test
	void ofWhenPomToNonPomOrdersPomLast() {
		assertThatFileSetIsOrdered("foo.jar", "foo.pom");
	}

	@Test
	void ofWhenIdenticalPathAndExceptionOrdersOnName() {
		assertThat(fileSetOf("foo/bar.jar", "foo/bar-a.jar", "foo/bar-b.jar"))
				.satisfies(filesNamed("bar.jar", "bar-a.jar", "bar-b.jar"));
	}

	@Test
	void ofWhenNoFileExtensionOrdersOnName() {
		assertThatFileSetIsOrdered("foo/bar", "foo/bar.jar");
	}

	@Test
	void ofOrdersFilesCorrectly() {
		List<String> names = new ArrayList<>();
		names.add("com/example/project/foo/2.0.0/foo-2.0.0-sources.jar");
		names.add("com/example/project/foo/2.0.0/foo-2.0.0.jar");
		names.add("com/example/project/bar/2.0.0/bar-2.0.0-sources.jar");
		names.add("com/example/project/foo/2.0.0/foo-2.0.0.pom");
		names.add("com/example/project/foo/2.0.0/maven-metadata.xml");
		names.add("com/example/project/bar/2.0.0/bar-2.0.0.pom");
		names.add("com/example/project/foo/2.0.0/foo-2.0.0-javadoc.jar");
		names.add("com/example/project/bar/2.0.0/bar-2.0.0.jar");
		names.add("com/example/project/bar/2.0.0/bar-2.0.0-javadoc.jar");
		names.add("com/example/project/bar/2.0.0/maven-metadata-local.xml");
		FileSet fileSet = fileSetOf(names);
		assertThat(fileSet).satisfies(filesNamed("bar-2.0.0.jar", "bar-2.0.0.pom", "maven-metadata-local.xml",
				"bar-2.0.0-javadoc.jar", "bar-2.0.0-sources.jar", "foo-2.0.0.jar", "foo-2.0.0.pom",
				"maven-metadata.xml", "foo-2.0.0-javadoc.jar", "foo-2.0.0-sources.jar"));
	}

	@Test
	void ofWhenHasShorterHiddenFileOrdersFilesCorrectly() {
		List<String> names = new ArrayList<>();
		String folder = "com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/";
		names.add(folder + ".foo.bar");
		names.add(folder + "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.jar");
		names.add(folder + "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-javadoc.jar");
		names.add(folder + "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-sources.jar");
		names.add(folder + "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.pom");
		FileSet fileSet = fileSetOf(names);
		assertThat(fileSet).satisfies(filesNamed("spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.pom", ".foo.bar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-javadoc.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-sources.jar"));
	}

	@Test // gh-4
	void ofWhenUsingTypicalOutputWorksInSort() throws Exception {
		List<String> names = readNames(getClass().getResourceAsStream("typical.txt"));
		FileSet fileSet = fileSetOf(names).filter(this::filter);
		assertThat(fileSet).satisfies(filesNamed("spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.pom",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-javadoc.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-sources.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.pom",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-javadoc.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-sources.jar",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.jar",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.pom",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1-sources.jar"));
	}

	@Test
	void filterFiltersFiles() {
		FileSet fileSet = fileSetOf("test.jar", "test.md5").filter(this::filter);
		assertThat(fileSet).satisfies(filesNamed("test.jar"));
	}

	@Test
	void batchedByCategoryReturnsBatchedFiles() throws Exception {
		List<String> names = readNames(getClass().getResourceAsStream("typical.txt"));
		FileSet fileSet = fileSetOf(names).filter(this::filter);
		MultiValueMap<Category, File> batched = fileSet.batchedByCategory();
		assertThat((Iterable<File>) batched.get(Category.PRIMARY))
				.satisfies(filesNamed("spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.jar",
						"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.jar",
						"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.jar"));
		assertThat((Iterable<File>) batched.get(Category.POM))
				.satisfies(filesNamed("spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.pom",
						"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.pom",
						"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.pom"));
		assertThat(batched.get(Category.MAVEN_METADATA)).isNull();
		assertThat((Iterable<File>) batched.get(Category.ADDITIONAL))
				.satisfies(filesNamed("spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-javadoc.jar",
						"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-sources.jar",
						"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-javadoc.jar",
						"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-sources.jar",
						"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1-sources.jar"));
	}

	private boolean filter(File file) {
		String name = file.getName().toLowerCase();
		return (!name.endsWith(".md5") && !name.endsWith("sha1") && !name.equalsIgnoreCase("maven-metadata.xml"));
	}

	private void assertThatFileSetIsOrdered(String... names) {
		File[] expected = Arrays.stream(names).map(this::makeFile).toArray(File[]::new);
		FileSet fileSet = fileSetOf(names);
		assertThat(fileSet).containsExactly(expected);
	}

	private FileSet fileSetOf(List<String> names) {
		return fileSetOf(StringUtils.toStringArray(names));
	}

	private FileSet fileSetOf(String... names) {
		File[] files = Arrays.stream(names).map(this::makeFile).toArray(File[]::new);
		FileSet fileSet = FileSet.of(files);
		File[] reversedFiles = files.clone();
		Collections.reverse(Arrays.asList(reversedFiles));
		FileSet reversedFileSet = FileSet.of(reversedFiles);
		assertThat(fileSet).containsExactlyElementsOf(reversedFileSet);
		return fileSet;
	}

	private List<String> readNames(InputStream inputStream) throws IOException {
		List<String> names = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String name = reader.readLine();
		while (name != null) {
			names.add(name);
			name = reader.readLine();
		}
		reader.close();
		return names;
	}

	private File makeFile(String path) {
		String tempPath = this.tempDir.getAbsolutePath();
		return new File(StringUtils.cleanPath(tempPath) + "/" + StringUtils.cleanPath(path));
	}

	private Consumer<Iterable<? extends File>> filesNamed(String... names) {
		return (iterable) -> {
			Iterator<? extends File> iterator = iterable.iterator();
			for (String name : names) {
				assertThat(iterator.next()).hasName(name);
			}
		};
	}

}
