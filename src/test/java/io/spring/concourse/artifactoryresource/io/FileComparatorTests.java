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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FileComparator}.
 *
 * @author Phillip Webb
 */
public class FileComparatorTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void compareOrdersOnParentPath() throws Exception {
		assertThat(compare("bar/bar.jar", "foo/bar.jar")).isLessThan(0);
		assertThat(compare("foo/bar.jar", "bar/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void compareWhenIdenticalPathOrdersOnExtension() throws Exception {
		assertThat(compare("foo/bar.jar", "foo/bar.war")).isLessThan(0);
		assertThat(compare("foo/bar.war", "foo/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void comparePomToNonPomOrdersPomLast() throws Exception {
		assertThat(compare("foo.pom", "foo.jar")).isGreaterThan(0);
	}

	@Test
	public void compareNonPomToPomOrdersPomLast() throws Exception {
		assertThat(compare("foo.jar", "foo.pom")).isLessThan(0);
	}

	@Test
	public void compareWhenIdenticalPathAndExceptionOrdersOnName() throws Exception {
		assertThat(compare("foo/bar.jar", "foo/zar.jar")).isLessThan(0);
		assertThat(compare("foo/zar.jar", "foo/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void compareWhenNoFileExtensionOrdersOnName() throws Exception {
		assertThat(compare("foo/bar", "foo/bar.jar")).isLessThan(0);
		assertThat(compare("foo/bar.jar", "foo/bar")).isGreaterThan(0);
	}

	@Test
	public void compareWorksInSort() throws Exception {
		List<File> files = new ArrayList<>();
		files.add(makeFile("com/example/project/foo/2.0.0/foo-2.0.0-sources.jar"));
		files.add(makeFile("com/example/project/foo/2.0.0/foo-2.0.0.jar"));
		files.add(makeFile("com/example/project/bar/2.0.0/bar-2.0.0-sources.jar"));
		files.add(makeFile("com/example/project/foo/2.0.0/foo-2.0.0.pom"));
		files.add(makeFile("com/example/project/foo/2.0.0/maven-metadata.xml"));
		files.add(makeFile("com/example/project/bar/2.0.0/bar-2.0.0.pom"));
		files.add(makeFile("com/example/project/foo/2.0.0/foo-2.0.0-javadoc.jar"));
		files.add(makeFile("com/example/project/bar/2.0.0/bar-2.0.0.jar"));
		files.add(makeFile("com/example/project/bar/2.0.0/bar-2.0.0-javadoc.jar"));
		files.add(makeFile("com/example/project/bar/2.0.0/maven-metadata-local.xml"));
		FileComparator.sort(files);
		List<String> names = files.stream().map(File::getName)
				.collect(Collectors.toCollection(ArrayList::new));
		assertThat(names).containsExactly("bar-2.0.0.jar", "bar-2.0.0.pom",
				"maven-metadata-local.xml", "bar-2.0.0-javadoc.jar",
				"bar-2.0.0-sources.jar", "foo-2.0.0.jar", "foo-2.0.0.pom",
				"maven-metadata.xml", "foo-2.0.0-javadoc.jar", "foo-2.0.0-sources.jar");
	}

	@Test
	public void compareWhenHasShorterHiddenFileWorksInSort() throws Exception {
		List<File> files = new ArrayList<>();
		files.add(makeFile(
				"com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/"
						+ ".foo.bar"));
		files.add(makeFile(
				"com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/"
						+ "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.jar"));
		files.add(makeFile(
				"com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/"
						+ "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-javadoc.jar"));
		files.add(makeFile(
				"com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/"
						+ "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-sources.jar"));
		files.add(makeFile(
				"com/example/project/spring-boot-actuator-autoconfigure/2.0.0.BUILD-SNAPSHOT/"
						+ "spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.pom"));
		FileComparator.sort(files);
		List<String> names = files.stream().map(File::getName)
				.collect(Collectors.toCollection(ArrayList::new));
		assertThat(names).containsExactly(
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT.pom", ".foo.bar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-javadoc.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-SNAPSHOT-sources.jar");
	}

	@Test
	public void compareWhenUsingTypicalOutputWorksInSort() throws Exception {
		// gh-4
		List<File> files = makeFiles(getClass().getResourceAsStream("typical.txt"));
		FileComparator.sort(files);
		List<String> names = files.stream().filter(this::filter).map(File::getName)
				.collect(Collectors.toCollection(ArrayList::new));
		assertThat(names).containsExactly(
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1.pom",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-javadoc.jar",
				"spring-boot-actuator-autoconfigure-2.0.0.BUILD-20171030.171822-1-sources.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1.pom",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-javadoc.jar",
				"spring-boot-actuator-2.0.0.BUILD-20171030.171543-1-sources.jar",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.jar",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1.pom",
				"spring-boot-starter-actuator-2.0.0.BUILD-20171030.172553-1-sources.jar");
	}

	private boolean filter(File file) {
		String name = file.getName().toLowerCase();
		return (!name.endsWith(".md5") && !name.endsWith("sha1")
				&& !name.equalsIgnoreCase("maven-metadata.xml"));
	}

	private int compare(String name1, String name2) throws IOException {
		return new FileComparator(Collections.emptyMap()).compare(makeFile(name1),
				makeFile(name2));
	}

	private List<File> makeFiles(InputStream inputStream) throws IOException {
		List<File> files = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = reader.readLine()) != null) {
			files.add(makeFile(line));
		}
		reader.close();
		return files;
	}

	private File makeFile(String path) {
		String tempPath = this.temporaryFolder.getRoot().getAbsolutePath();
		return new File(
				StringUtils.cleanPath(tempPath) + "/" + StringUtils.cleanPath(path));
	}

}
