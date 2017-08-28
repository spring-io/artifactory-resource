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

package io.spring.concourse.artifactoryresource.command;

import io.spring.concourse.artifactoryresource.artifactory.BuildModulesGenerator;
import io.spring.concourse.artifactoryresource.maven.MavenBuildModulesGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ModuleLayouts}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ModuleLayoutsTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ModuleLayouts moduleLayouts = new ModuleLayouts();

	@Test
	public void getBuildModulesGeneratorWhenLayoutIsNullShouldReturnMaven()
			throws Exception {
		assertThat(this.moduleLayouts.getBuildModulesGenerator(null))
				.isInstanceOf(MavenBuildModulesGenerator.class);
	}

	@Test
	public void getBuildModulesGeneratorWhenLayoutIsEmptyShouldReturnMaven()
			throws Exception {
		assertThat(this.moduleLayouts.getBuildModulesGenerator(""))
				.isInstanceOf(MavenBuildModulesGenerator.class);
	}

	@Test
	public void getBuildModulesGeneratorWhenLayoutIsMavenShouldReturnMaven()
			throws Exception {
		assertThat(this.moduleLayouts.getBuildModulesGenerator("mAvEN"))
				.isInstanceOf(MavenBuildModulesGenerator.class);
	}

	@Test
	public void getBuildModulesGeneratorWhenLayoutIsNoneShouldReturnNone()
			throws Exception {
		assertThat(this.moduleLayouts.getBuildModulesGenerator("none"))
				.isSameAs(BuildModulesGenerator.NONE);
	}

	@Test
	public void getBuildModulesGeneratorWhenLayoutIsUnknownShouldThrowException()
			throws Exception {
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Unknown module layout 'foo'");
		this.moduleLayouts.getBuildModulesGenerator("foo");
	}

}
