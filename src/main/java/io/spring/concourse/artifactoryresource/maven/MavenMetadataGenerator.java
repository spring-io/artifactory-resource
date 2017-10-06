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

package io.spring.concourse.artifactoryresource.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.io.DirectoryScanner;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * Generate Maven metadata files for downloaded artifacts.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class MavenMetadataGenerator {

	private static final List<String> POM_PATTERN = Collections
			.unmodifiableList(Collections.singletonList("**/*.pom"));

	private static final Set<String> IGNORED_EXTENSIONS = Collections
			.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("asc", "sha", "md5")));

	private final DirectoryScanner scanner;

	public MavenMetadataGenerator(DirectoryScanner scanner) {
		this.scanner = scanner;
	}

	public void generate(Directory root) {
		List<File> pomFiles = this.scanner.scan(root, POM_PATTERN);
		pomFiles.forEach((pomFile) -> generate(root, pomFile));
	}

	private void generate(Directory root, File pomFile) {
		String name = StringUtils.getFilename(pomFile.getName());
		String extension = StringUtils.getFilenameExtension(pomFile.getName());
		String prefix = name.substring(0, name.length() - extension.length() - 1);
		File[] files = pomFile.getParentFile().listFiles((f) -> include(f, prefix));
		MultiValueMap<File, MavenCoordinates> coordinates = new LinkedMultiValueMap<>();
		for (File file : files) {
			String rootPath = StringUtils.cleanPath(root.getFile().getPath());
			String relativePath = StringUtils.cleanPath(file.getPath())
					.substring(rootPath.length() + 1);
			coordinates.add(file.getParentFile(),
					MavenCoordinates.fromPath(relativePath));
		}
		coordinates.forEach(this::writeMetadata);
	}

	private boolean include(File file, String prefix) {
		String extension = StringUtils.getFilenameExtension(file.getName());
		if (IGNORED_EXTENSIONS.contains(extension.toLowerCase())) {
			return false;
		}
		return file.exists() && file.isFile()
				&& StringUtils.getFilename(file.getName()).startsWith(prefix);
	}

	private void writeMetadata(File folder, List<MavenCoordinates> coordinates) {
		List<SnapshotVersion> snapshotVersions = getSnapshotVersionMetadata(coordinates);
		if (!snapshotVersions.isEmpty()) {
			Metadata metadata = new Metadata();
			Versioning versioning = new Versioning();
			versioning.setSnapshotVersions(snapshotVersions);
			metadata.setVersioning(versioning);
			metadata.setGroupId(coordinates.get(0).getGroupId());
			metadata.setArtifactId(coordinates.get(0).getArtifactId());
			metadata.setVersion(coordinates.get(0).getVersion());
			writeMetadataFile(metadata, new File(folder, "maven-metadata.xml"));
		}
	}

	private List<SnapshotVersion> getSnapshotVersionMetadata(
			List<MavenCoordinates> coordinates) {
		return coordinates.stream().filter(MavenCoordinates::isSnapshotVersion).sorted()
				.map(this::asSnapshotVersionMetadata)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private SnapshotVersion asSnapshotVersionMetadata(MavenCoordinates coordinates) {
		SnapshotVersion snapshotVersion = new SnapshotVersion();
		snapshotVersion.setClassifier(coordinates.getClassifier());
		snapshotVersion.setExtension(coordinates.getExtension());
		snapshotVersion.setVersion(coordinates.getSnapshotVersion());
		return snapshotVersion;
	}

	private void writeMetadataFile(Metadata metadata, File file) {
		try {
			MetadataXpp3Writer writer = new MetadataXpp3Writer();
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				writer.write(outputStream, metadata);
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
