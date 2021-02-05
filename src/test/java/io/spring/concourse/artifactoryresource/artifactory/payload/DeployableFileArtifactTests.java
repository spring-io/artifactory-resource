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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link DeployableFileArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class DeployableFileArtifactTests extends AbstractDeployableArtifactTests {

	@TempDir
	File tempDir;

	@Test
	public void createWhenParentIsNotParentThrowsException() {
		File parent = new File(this.tempDir, "parent");
		File file = new File(this.tempDir, "test");
		assertThatIllegalArgumentException().isThrownBy(() -> new DeployableFileArtifact(parent, file))
				.withMessageContaining("is not a parent of");
	}

	@Override
	protected AbstractDeployableArtifact create(String path, byte[] content, Map<String, String> properties,
			Checksums checksums) throws IOException {
		File file = new File(this.tempDir.getAbsolutePath() + File.separatorChar + path.replace("/", File.separator));
		file.getParentFile().mkdirs();
		FileCopyUtils.copy(content, file);
		return new DeployableFileArtifact(this.tempDir, file, properties, checksums);
	}

}
