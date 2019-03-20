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

package io.spring.concourse.artifactoryresource.maven;

import java.util.ArrayList;
import java.util.List;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableByteArrayArtifact;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenBuildModulesGenerator}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class MavenBuildModulesGeneratorTests {

	private static final byte[] NO_CONTENT = {};

	private MavenBuildModulesGenerator generator = new MavenBuildModulesGenerator();

	@Test
	public void getBuildModulesReturnsBuildModules() {
		List<DeployableArtifact> deployableArtifacts = new ArrayList<>();
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.pom"));
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.jar"));
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0-sources.jar"));
		deployableArtifacts.add(artifact("/com/example/bar/1.0.0/bar-1.0.0.pom"));
		deployableArtifacts.add(artifact("/com/example/bar/1.0.0/bar-1.0.0.jar"));
		deployableArtifacts.add(artifact("/com/example/bar/1.0.0/bar-1.0.0-sources.jar"));
		List<BuildModule> buildModules = this.generator
				.getBuildModules(deployableArtifacts);
		assertThat(buildModules).hasSize(2);
		assertThat(buildModules.get(0).getId()).isEqualTo("com.example:foo:1.0.0");
		assertThat(buildModules.get(0).getArtifacts()).extracting(BuildArtifact::getName)
				.containsExactly("foo-1.0.0.pom", "foo-1.0.0.jar",
						"foo-1.0.0-sources.jar");
		assertThat(buildModules.get(0).getArtifacts()).extracting(BuildArtifact::getType)
				.containsExactly("pom", "jar", "java-source-jar");
		assertThat(buildModules.get(1).getId()).isEqualTo("com.example:bar:1.0.0");
		assertThat(buildModules.get(1).getArtifacts()).extracting(BuildArtifact::getName)
				.containsExactly("bar-1.0.0.pom", "bar-1.0.0.jar",
						"bar-1.0.0-sources.jar");
		assertThat(buildModules.get(1).getArtifacts()).extracting(BuildArtifact::getType)
				.containsExactly("pom", "jar", "java-source-jar");
	}

	@Test
	public void getBuildModulesWhenContainingSpecificExtensionsFiltersArtifacts()
			throws Exception {
		List<DeployableArtifact> deployableArtifacts = new ArrayList<>();
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.pom"));
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.asc"));
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.md5"));
		deployableArtifacts.add(artifact("/com/example/foo/1.0.0/foo-1.0.0.sha"));
		List<BuildModule> buildModules = this.generator
				.getBuildModules(deployableArtifacts);
		assertThat(buildModules.get(0).getArtifacts()).extracting(BuildArtifact::getName)
				.containsExactly("foo-1.0.0.pom");
	}

	@Test
	public void getBuildModulesWhenContainingUnexpectedLayoutReturnsEmptyList()
			throws Exception {
		List<DeployableArtifact> deployableArtifacts = new ArrayList<>();
		deployableArtifacts.add(artifact("/foo-1.0.0.zip"));
		List<BuildModule> buildModules = this.generator
				.getBuildModules(deployableArtifacts);
		assertThat(buildModules).isEmpty();
	}

	private DeployableByteArrayArtifact artifact(String path) {
		return new DeployableByteArrayArtifact(path, NO_CONTENT);
	}

}
