/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.io.DirectoryScanner;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenMetadataGenerator}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class MavenMetadataGeneratorTests {

	private static final byte[] NO_BYTES = {};

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private MavenMetadataGenerator generator = new MavenMetadataGenerator(
			new DirectoryScanner());

	@Test
	public void generateWhenUsingNonSnapshotDoesNotCreateMetadata() throws Exception {
		Directory directory = createStructure("1.0.0.RELEASE");
		this.generator.generate(directory, false);
		File file = new File(directory.toString()
				+ "/com/example/project/my-project/1.0.0.RELEASE/maven-metadata.xml");
		assertThat(file).doesNotExist();
	}

	@Test
	public void generateWhenUsingSnapshotCreatesMetadata() throws Exception {
		Directory directory = createStructure("1.0.0.BUILD-SNAPSHOT");
		this.generator.generate(directory, false);
		File file = new File(directory.toString()
				+ "/com/example/project/my-project/1.0.0.BUILD-SNAPSHOT/maven-metadata.xml");
		URL expected = getClass().getResource("generate-when-using-snapshot.xml");
		assertThat(file).exists().has(xmlContent(expected));
	}

	@Test
	public void generateWhenUsingSnapshotTimestampCreatesMetadata() throws Exception {
		Directory directory = createStructure("1.0.0.BUILD-SNAPSHOT",
				"1.0.0.BUILD-20170626.200218-328");
		this.generator.generate(directory, false);
		File file = new File(directory.toString()
				+ "/com/example/project/my-project/1.0.0.BUILD-SNAPSHOT/maven-metadata.xml");
		URL expected = getClass()
				.getResource("generate-when-using-snapshot-timestamp.xml");
		assertThat(file).exists().has(xmlContent(expected));
	}

	@Test
	public void generateWhenCreatingChecksumsCreatesChecksums() throws Exception {
		Directory directory = createStructure("1.0.0.BUILD-SNAPSHOT");
		this.generator.generate(directory, true);
		File folder = new File(directory.toString()
				+ "/com/example/project/my-project/1.0.0.BUILD-SNAPSHOT/");
		assertThat(new File(folder, "maven-metadata.xml.md5")).exists();
		assertThat(new File(folder, "maven-metadata.xml.sha1")).exists();
	}

	private Directory createStructure(String version) throws IOException {
		return createStructure(version, version);
	}

	private Directory createStructure(String folderVersion, String fileVersion)
			throws IOException {
		Directory root = new Directory(this.temporaryFolder.newFolder());
		String prefix = "com/example/project/my-project/";
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + ".pom");
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + ".jar");
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + ".asc");
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + ".sha");
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + ".md5");
		add(root, prefix + folderVersion + "/my-project-" + fileVersion + "-sources.jar");
		add(root, prefix + folderVersion + "/other" + fileVersion + ".jar");
		return root;
	}

	private void add(Directory root, String path) throws IOException {
		File file = new File(root.getFile(), path);
		file.getParentFile().mkdirs();
		FileCopyUtils.copy(NO_BYTES, file);
	}

	private Condition<File> xmlContent(URL expected) {
		return new Condition<File>("XML Content") {

			@Override
			public boolean matches(File actual) {
				Diff diff = DiffBuilder.compare(Input.from(expected))
						.withTest(Input.from(actual)).checkForSimilar().ignoreWhitespace()
						.build();
				if (diff.hasDifferences()) {
					try {
						String content = new String(
								FileCopyUtils.copyToByteArray(actual));
						throw new AssertionError(diff.toString() + "\n" + content);
					}
					catch (IOException ex) {
						throw new IllegalStateException(ex);
					}
				}
				return true;
			}

		};
	}

}
