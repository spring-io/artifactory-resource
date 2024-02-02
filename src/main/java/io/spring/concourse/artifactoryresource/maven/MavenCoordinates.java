/*
 * Copyright 2017-2024 the original author or authors.
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

package io.spring.concourse.artifactoryresource.maven;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Maven coordinates (group/artifact/version etc).
 *
 * @author Phillip Webb
 */
public final class MavenCoordinates implements Comparable<MavenCoordinates> {

	private static final String SNAPSHOT = "SNAPSHOT";

	private static final String SNAPSHOT_SUFFIX = "-" + SNAPSHOT;

	private static final Pattern FOLDER_PATTERN = Pattern.compile("(.*)\\/(.*)\\/(.*)\\/(.*)");

	private static final Pattern VERSION_FILE_PATTERN = Pattern.compile("^([0-9]{8}.[0-9]{6})-([0-9]+)(.*)$");

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String classifier;

	private final String extension;

	private final String snapshotVersion;

	private MavenCoordinates(String groupId, String artifactId, String version, String classifier, String extension,
			String snapshotVersion) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.extension = extension;
		this.snapshotVersion = snapshotVersion;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getVersion() {
		return this.version;
	}

	public String getClassifier() {
		return this.classifier;
	}

	public String getExtension() {
		return this.extension;
	}

	public String getSnapshotVersion() {
		return this.snapshotVersion;
	}

	public boolean isSnapshotVersion() {
		return getVersionType() != MavenVersionType.FIXED;
	}

	public MavenVersionType getVersionType() {
		return MavenVersionType.fromVersion(this.snapshotVersion);
	}

	@Override
	public String toString() {
		return this.groupId + ":" + this.artifactId + ":" + this.version + ":" + this.classifier + ":" + this.version
				+ ":" + this.snapshotVersion;
	}

	@Override
	public int compareTo(MavenCoordinates o) {
		return Comparator.comparing(MavenCoordinates::getGroupId)
			.thenComparing(MavenCoordinates::getArtifactId)
			.thenComparing(MavenCoordinates::getVersion)
			.thenComparing(MavenCoordinates::getExtension)
			.thenComparing(MavenCoordinates::getClassifier)
			.compare(this, o);
	}

	public static MavenCoordinates fromPath(String path) {
		try {
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			Matcher folderMatcher = FOLDER_PATTERN.matcher(path);
			Assert.state(folderMatcher.matches(), "Path does not match folder pattern");
			String groupId = folderMatcher.group(1).replace('/', '.');
			String artifactId = folderMatcher.group(2);
			String version = folderMatcher.group(3);
			String rootVersion = (version.endsWith(SNAPSHOT_SUFFIX)
					? version.substring(0, version.length() - SNAPSHOT_SUFFIX.length()) : version);
			String name = folderMatcher.group(4);
			Assert.state(name.startsWith(artifactId),
					"Name '" + name + "' does not start with artifact ID '" + artifactId + "'");
			String snapshotVersionAndClassifier = name.substring(artifactId.length() + 1);
			String extension = StringUtils.getFilenameExtension(snapshotVersionAndClassifier);
			snapshotVersionAndClassifier = snapshotVersionAndClassifier.substring(0,
					snapshotVersionAndClassifier.length() - extension.length() - 1);
			String classifier = snapshotVersionAndClassifier;
			if (classifier.startsWith(rootVersion)) {
				classifier = classifier.substring(rootVersion.length());
				classifier = stripDash(classifier);
			}
			Matcher versionMatcher = VERSION_FILE_PATTERN.matcher(classifier);
			if (versionMatcher.matches()) {
				classifier = versionMatcher.group(3);
				classifier = stripDash(classifier);
			}
			if (classifier.startsWith(SNAPSHOT)) {
				classifier = classifier.substring(SNAPSHOT.length());
				classifier = stripDash(classifier);
			}
			String snapshotVersion = (classifier.isEmpty() ? snapshotVersionAndClassifier : snapshotVersionAndClassifier
				.substring(0, snapshotVersionAndClassifier.length() - classifier.length() - 1));
			return new MavenCoordinates(groupId, artifactId, version, classifier, extension, snapshotVersion);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to parse maven coordinates from path '" + path + "'", ex);
		}
	}

	private static String stripDash(String classifier) {
		if (classifier.startsWith("-")) {
			return classifier.substring(1);
		}
		return classifier;
	}

}
