/*
 * Copyright 2017-2021 the original author or authors.
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

package io.spring.concourse.artifactoryresource.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableByteArrayArtifact;
import io.spring.concourse.artifactoryresource.io.FileSet.Category;
import io.spring.concourse.artifactoryresource.openpgp.ArmoredAsciiSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link DeployableArtifactsSigner}.
 *
 * @author Phillip Webb
 */
class DeployableArtifactsSignerTests {

	private DeployableArtifactsSigner signer;

	private Map<String, String> properties;

	@BeforeEach
	void setup() throws IOException {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner
				.get(ArmoredAsciiSigner.class.getResourceAsStream("test-private.txt"), "password");
		this.properties = Collections.singletonMap("test", "param");
		this.signer = new DeployableArtifactsSigner(signer, this.properties);
	}

	@Test
	void signWhenAlreadyContainsSignedFilesThrowsException() {
		MultiValueMap<Category, DeployableArtifact> batchedArtifacts = new LinkedMultiValueMap<>();
		batchedArtifacts.add(Category.SIGNATURE, new DeployableByteArrayArtifact("/file.asc", new byte[0]));
		assertThatIllegalStateException().isThrownBy(() -> this.signer.sign(batchedArtifacts))
				.withMessage("Files must not already be signed");
	}

	@Test
	void signAddsSignedFiles() throws Exception {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/com/example/myapp.jar",
				"test".getBytes(StandardCharsets.UTF_8));
		MultiValueMap<Category, DeployableArtifact> batchedArtifacts = new LinkedMultiValueMap<>();
		batchedArtifacts.add(Category.PRIMARY, artifact);
		MultiValueMap<Category, DeployableArtifact> signed = this.signer.sign(batchedArtifacts);
		assertThat(signed.getFirst(Category.PRIMARY)).isEqualTo(artifact);
		DeployableArtifact signatureResource = signed.getFirst(Category.SIGNATURE);
		assertThat(signatureResource.getPath()).isEqualTo("/com/example/myapp.jar.asc");
		assertThat(FileCopyUtils.copyToByteArray(signatureResource.getContent().getInputStream()))
				.asString(StandardCharsets.UTF_8).contains("PGP SIGNATURE");
		assertThat(signatureResource.getSize()).isGreaterThan(10);
		assertThat(signatureResource.getProperties()).isEqualTo(this.properties);
		assertThat(signatureResource.getChecksums()).isNotNull();
	}

}
