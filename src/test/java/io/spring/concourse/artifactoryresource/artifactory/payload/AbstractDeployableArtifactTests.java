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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AbstractDeployableArtifact}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public abstract class AbstractDeployableArtifactTests {

	private static final byte[] CONTENT = "abc".getBytes();

	@Test
	public void createWhenPropertiesIsNullUsesEmptyProperties() throws Exception {
		AbstractDeployableArtifact artifact = create("/foo", CONTENT, null, null);
		assertThat(artifact.getProperties()).isNotNull().isEmpty();
	}

	@Test
	public void createWhenChecksumIsNullCalculatesChecksums() throws Exception {
		AbstractDeployableArtifact artifact = create("/foo", CONTENT, null, null);
		assertThat(artifact.getChecksums().getSha1())
				.isEqualTo("a9993e364706816aba3e25717850c26c9cd0d89d");
		assertThat(artifact.getChecksums().getMd5())
				.isEqualTo("900150983cd24fb0d6963f7d28e17f72");
	}

	@Test
	public void getPropertiesReturnsProperties() throws Exception {
		Map<String, String> properties = Collections.singletonMap("foo", "bar");
		AbstractDeployableArtifact artifact = create("/foo", CONTENT, properties, null);
		assertThat(artifact.getProperties()).isEqualTo(properties);
	}

	@Test
	public void getChecksumReturnsChecksum() throws Exception {
		Checksums checksums = new Checksums("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		AbstractDeployableArtifact artifact = create("/foo", CONTENT, null, checksums);
		assertThat(artifact.getChecksums()).isEqualTo(checksums);
	}

	@Test
	public void getPathReturnsPath() throws Exception {
		AbstractDeployableArtifact artifact = create("/foo/bar", CONTENT, null, null);
		assertThat(artifact.getPath()).isEqualTo("/foo/bar");
	}

	@Test
	public void getContentReturnsContent() throws Exception {
		AbstractDeployableArtifact artifact = create("/foo", CONTENT, null, null);
		assertThat(FileCopyUtils.copyToByteArray(artifact.getContent().getInputStream()))
				.isEqualTo(CONTENT);
	}

	protected abstract AbstractDeployableArtifact create(String path, byte[] content,
			Map<String, String> properties, Checksums checksums) throws IOException;

}
