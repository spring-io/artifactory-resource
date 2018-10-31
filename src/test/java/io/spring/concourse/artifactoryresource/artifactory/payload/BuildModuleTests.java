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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildModule}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@RunWith(SpringRunner.class)
@JsonTest
public class BuildModuleTests {

	private static final String ID = "com.example.module:my-module:1.0.0-SNAPSHOT";

	private static final BuildArtifact BUILD_ARTIFACT = new BuildArtifact("jar",
			"a9993e364706816aba3e25717850c26c9cd0d89d",
			"900150983cd24fb0d6963f7d28e17f72", "foo.jar");

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private JacksonTester<BuildModule> json;

	@Test
	public void createWhenIdIsEmptyShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("ID must not be empty");
		new BuildModule("", null);
	}

	@Test
	public void createWhenArtifactsIsEmptyShouldUseEmptyList() throws Exception {
		BuildModule module = new BuildModule(ID, null);
		assertThat(module.getArtifacts()).isNotNull().isEmpty();
	}

	@Test
	public void writeShouldSerialize() throws Exception {
		BuildModule module = new BuildModule(ID,
				Collections.singletonList(BUILD_ARTIFACT));
		assertThat(this.json.write(module)).isEqualToJson("build-module.json");
	}

}
