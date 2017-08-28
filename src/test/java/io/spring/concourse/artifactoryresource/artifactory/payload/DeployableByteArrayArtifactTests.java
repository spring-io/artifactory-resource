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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.io.IOException;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link DeployableByteArrayArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class DeployableByteArrayArtifactTests extends AbstractDeployableArtifactTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createWhenBytesIsNullShouldThrowException() throws Exception {
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("Content must not be null");
		new DeployableByteArrayArtifact("foo", null);
	}

	@Override
	protected AbstractDeployableArtifact create(String path, byte[] content,
			Map<String, String> properties, Checksums checksums) throws IOException {
		return new DeployableByteArrayArtifact(path, content, properties, checksums);
	}

}
