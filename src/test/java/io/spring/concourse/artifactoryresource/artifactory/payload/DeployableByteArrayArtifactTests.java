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

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link DeployableByteArrayArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class DeployableByteArrayArtifactTests extends AbstractDeployableArtifactTests {

	@Test
	public void createWhenBytesIsNullThrowsException() throws Exception {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new DeployableByteArrayArtifact("/foo", null))
				.withMessage("Content must not be null");
	}

	@Override
	protected AbstractDeployableArtifact create(String path, byte[] content,
			Map<String, String> properties, Checksums checksums) throws IOException {
		return new DeployableByteArrayArtifact(path, content, properties, checksums);
	}

}
