/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
	public void compareShouldOrderOnParentPath() throws Exception {
		assertThat(compare("bar/bar.jar", "foo/bar.jar")).isLessThan(0);
		assertThat(compare("foo/bar.jar", "bar/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void compareWhenIdenticalPathShouldOrderOnExtension() throws Exception {
		assertThat(compare("foo/bar.jar", "foo/bar.war")).isLessThan(0);
		assertThat(compare("foo/bar.war", "foo/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void comparePomToNonPomShouldOrderPomFirst() throws Exception {
		assertThat(compare("foo.pom", "foo.jar")).isLessThan(0);
	}

	@Test
	public void compareNonPomToPomShouldOrderPomFirst() throws Exception {
		assertThat(compare("foo.jar", "foo.pom")).isGreaterThan(0);
	}

	@Test
	public void compareWhenIdenticalPathAndExceptionShouldOrderOnName() throws Exception {
		assertThat(compare("foo/bar.jar", "foo/zar.jar")).isLessThan(0);
		assertThat(compare("foo/zar.jar", "foo/bar.jar")).isGreaterThan(0);
	}

	@Test
	public void compareShouldWorkInSort() throws Exception {
		List<File> files = new ArrayList<>();
		files.add(makeFile("foo-sources.jar"));
		files.add(makeFile("foo.jar"));
		files.add(makeFile("foo.pom"));
		files.add(makeFile("foo-javadoc.jar"));
		Collections.sort(files, FileComparator.INSTANCE);
		List<String> names = files.stream().map(File::getName)
				.collect(Collectors.toCollection(ArrayList::new));
		assertThat(names).containsExactly("foo.pom", "foo.jar", "foo-javadoc.jar",
				"foo-sources.jar");
	}

	private int compare(String name1, String name2) throws IOException {
		return FileComparator.INSTANCE.compare(makeFile(name1), makeFile(name2));
	}

	private File makeFile(String path) {
		String tempPath = this.temporaryFolder.getRoot().getAbsolutePath();
		return new File(
				StringUtils.cleanPath(tempPath) + "/" + StringUtils.cleanPath(path));
	}

}
