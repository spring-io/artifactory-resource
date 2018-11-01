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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link MavenCoordinates}.
 *
 * @author Phillip Webb
 */
public class MavenCoordinatesTests {

	@Test
	public void fromPathShouldReturnCoordinates() throws Exception {
		MavenCoordinates coordinates = MavenCoordinates.fromPath(
				"/com/example/project/" + "my-project/" + "1.0.0.BUILD-SNAPSHOT/"
						+ "my-project-1.0.0.BUILD-20171005.194031-1.jar");
		assertThat(coordinates.getGroupId()).isEqualTo("com.example.project");
		assertThat(coordinates.getArtifactId()).isEqualTo("my-project");
		assertThat(coordinates.getVersion()).isEqualTo("1.0.0.BUILD-SNAPSHOT");
		assertThat(coordinates.getClassifier()).isEqualTo("");
		assertThat(coordinates.getSnapshotVersion())
				.isEqualTo("1.0.0.BUILD-20171005.194031-1");
	}

	@Test
	public void fromPathWhenHasClassifierShouldReturnCoordinates() throws Exception {
		MavenCoordinates coordinates = MavenCoordinates.fromPath(
				"/com/example/project/" + "my-project/" + "1.0.0.BUILD-SNAPSHOT/"
						+ "my-project-1.0.0.BUILD-20171005.194031-1-sources.jar");
		assertThat(coordinates.getGroupId()).isEqualTo("com.example.project");
		assertThat(coordinates.getArtifactId()).isEqualTo("my-project");
		assertThat(coordinates.getVersion()).isEqualTo("1.0.0.BUILD-SNAPSHOT");
		assertThat(coordinates.getClassifier()).isEqualTo("sources");
		assertThat(coordinates.getSnapshotVersion())
				.isEqualTo("1.0.0.BUILD-20171005.194031-1");
	}

	@Test
	public void fromPathWhenReleaseShouldReturnCoordinates() throws Exception {
		MavenCoordinates coordinates = MavenCoordinates
				.fromPath("/com/example/project/" + "my-project/" + "1.0.0.RELEASE/"
						+ "my-project-1.0.0.RELEASE-sources.jar");
		assertThat(coordinates.getGroupId()).isEqualTo("com.example.project");
		assertThat(coordinates.getArtifactId()).isEqualTo("my-project");
		assertThat(coordinates.getVersion()).isEqualTo("1.0.0.RELEASE");
		assertThat(coordinates.getClassifier()).isEqualTo("sources");
		assertThat(coordinates.getSnapshotVersion()).isEqualTo("1.0.0.RELEASE");
	}

	@Test
	public void fromPathWhenIsBadThrowsNiceException() {
		// gh-5
		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> MavenCoordinates
						.fromPath("org/springframework/cloud/skipper/acceptance/app/"
								+ "skipper-server-with-drivers/maven-metadata-local.xml"))
				.withMessageContaining("Unable to parse maven coordinates from path")
				.withStackTraceContaining(
						"Name 'maven-metadata-local.xml' does not start with artifact ID 'app'");
	}

}
