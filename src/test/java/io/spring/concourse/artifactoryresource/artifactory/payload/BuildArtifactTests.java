/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link BuildArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@RunWith(SpringRunner.class)
@JsonTest
public class BuildArtifactTests {

	private static final String TYPE = "jar";

	private static final String SHA1 = "a9993e364706816aba3e25717850c26c9cd0d89d";

	private static final String MD5 = "900150983cd24fb0d6963f7d28e17f72";

	private static final String NAME = "foo.jar";

	@Autowired
	public JacksonTester<BuildArtifact> json;

	@Test
	public void createWhenTypeIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new BuildArtifact("", SHA1, MD5, NAME))
				.withMessage("Type must not be empty");
	}

	@Test
	public void createWhenSha1IsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new BuildArtifact(TYPE, "", MD5, NAME))
				.withMessage("SHA1 must not be empty");
	}

	@Test
	public void createWhenMd5IsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new BuildArtifact(TYPE, SHA1, "", NAME))
				.withMessage("MD5 must not be empty");
	}

	@Test
	public void createWhenNameIsEmptyThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new BuildArtifact(TYPE, SHA1, MD5, ""))
				.withMessage("Name must not be empty");
	}

	@Test
	public void writeSerializesJson() throws Exception {
		BuildArtifact artifact = new BuildArtifact(TYPE, SHA1, MD5, NAME);
		assertThat(this.json.write(artifact)).isEqualToJson("build-artifact.json");
	}

}
