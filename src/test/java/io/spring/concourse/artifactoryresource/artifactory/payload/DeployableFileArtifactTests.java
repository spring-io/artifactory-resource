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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link DeployableFileArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class DeployableFileArtifactTests extends AbstractDeployableArtifactTests {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void createWhenParentIsNotParentThrowsException() throws Exception {
		File parent = this.temp.newFolder();
		File file = this.temp.newFile();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new DeployableFileArtifact(parent, file))
				.withMessageContaining("is not a parent of");
	}

	@Override
	protected AbstractDeployableArtifact create(String path, byte[] content,
			Map<String, String> properties, Checksums checksums) throws IOException {
		File parent = this.temp.newFolder();
		File file = new File(parent.getAbsolutePath() + File.separatorChar
				+ path.replace("/", File.separator));
		file.getParentFile().mkdirs();
		FileCopyUtils.copy(content, file);
		return new DeployableFileArtifact(parent, file, properties, checksums);
	}

}
