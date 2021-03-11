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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryBuildRuns;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryRepository;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.command.payload.InRequest;
import io.spring.concourse.artifactoryresource.command.payload.InRequest.Params;
import io.spring.concourse.artifactoryresource.command.payload.InResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.maven.MavenMetadataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link InHandler}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InHandlerTests {

	private static final String BUILD_INFO_JSON = "{}";

	@TempDir
	File tempDir;

	@Mock
	private Artifactory artifactory;

	@Mock
	private MavenMetadataGenerator mavenMetadataGenerator;

	@Mock
	private ArtifactoryServer artifactoryServer;

	@Mock
	private ArtifactoryBuildRuns artifactoryBuildRuns;

	@Mock
	private ArtifactoryRepository artifactoryRepository;

	private List<DeployedArtifact> deployedArtifacts;

	private InHandler handler;

	@BeforeEach
	void setup() {
		this.deployedArtifacts = createDeployedArtifacts();
		given(this.artifactory.server("https://ci.example.com", "admin", "password", null))
				.willReturn(this.artifactoryServer);
		given(this.artifactoryServer.buildRuns("my-build")).willReturn(this.artifactoryBuildRuns);
		given(this.artifactoryServer.repository("libs-snapshot-local")).willReturn(this.artifactoryRepository);
		given(this.artifactoryBuildRuns.getDeployedArtifacts("1234")).willReturn(this.deployedArtifacts);
		given(this.artifactoryBuildRuns.getRawBuildInfo("1234")).willReturn(BUILD_INFO_JSON);
		this.handler = new InHandler(this.artifactory, this.mavenMetadataGenerator);
	}

	private List<DeployedArtifact> createDeployedArtifacts() {
		List<DeployedArtifact> deployedArtifacts = new ArrayList<>();
		deployedArtifacts.add(new DeployedArtifact("libs-snapshot-local", "foo.jar", "com/example"));
		deployedArtifacts.add(new DeployedArtifact("libs-snapshot-local", "bar.jar", "com/example"));
		return deployedArtifacts;
	}

	@Test
	void handleWhenDownloadArtifactsDownloadsArtifacts() {
		InRequest request = createRequest(false, false, true);
		Directory directory = new Directory(this.tempDir);
		InResponse response = this.handler.handle(request, directory);
		for (DeployedArtifact deployedArtifact : this.deployedArtifacts) {
			verify(this.artifactoryRepository).download(deployedArtifact, directory.getFile(), true);
		}
		assertThat(response.getVersion()).isEqualTo(request.getVersion());
	}

	@Test
	void handleWhenDownloadChecksumsFalseDownloadsArtifactsWithoutChecksums() {
		InRequest request = createRequest(false, false, true, false);
		Directory directory = new Directory(this.tempDir);
		InResponse response = this.handler.handle(request, directory);
		for (DeployedArtifact deployedArtifact : this.deployedArtifacts) {
			verify(this.artifactoryRepository).download(deployedArtifact, directory.getFile(), false);
		}
		assertThat(response.getVersion()).isEqualTo(request.getVersion());
	}

	@Test
	void handleWhenDownloadChecksumsTrueAndAscFileDownloadsArtifactsWithoutChecksums() {
		DeployedArtifact deployedArtifact = new DeployedArtifact("libs-snapshot-local", "foo.asc", "com/example");
		this.deployedArtifacts.clear();
		this.deployedArtifacts.add(deployedArtifact);
		InRequest request = createRequest(false, false, true);
		Directory directory = new Directory(this.tempDir);
		InResponse response = this.handler.handle(request, directory);
		verify(this.artifactoryRepository).download(deployedArtifact, directory.getFile(), false);
		assertThat(response.getVersion()).isEqualTo(request.getVersion());
	}

	@Test
	void handleWhenDownloadArtifactsFalseDoesNotDownloadArtifacts() {
		InRequest request = createRequest(false, false, false);
		Directory directory = new Directory(this.tempDir);
		InResponse response = this.handler.handle(request, directory);
		verifyNoInteractions(this.artifactoryRepository);
		verifyNoInteractions(this.mavenMetadataGenerator);
		assertThat(response.getVersion()).isEqualTo(request.getVersion());
	}

	@Test
	void handleWhenHasGenerateMavenMetadataParamGeneratesMetadata() {
		InRequest request = createRequest(true, false, true);
		Directory directory = new Directory(this.tempDir);
		this.handler.handle(request, directory);
		verify(this.mavenMetadataGenerator).generate(directory, true);
	}

	@Test
	void handleWhenHasNoGenerateMavenMetadataParamDoesNotGenerateMetadata() {
		InRequest request = createRequest(false, false, true);
		Directory directory = new Directory(this.tempDir);
		this.handler.handle(request, directory);
		verifyNoInteractions(this.mavenMetadataGenerator);
	}

	@Test
	void handleWhenHasNoDownloadChecksumParamGeneratesMetadataButNotChecksums() {
		InRequest request = createRequest(true, false, true, false);
		Directory directory = new Directory(this.tempDir);
		this.handler.handle(request, directory);
		verify(this.mavenMetadataGenerator).generate(directory, false);
	}

	@Test
	void handleWhenSaveBuildInfoSavesBuildInfo() {
		InRequest request = createRequest(false, true, true);
		Directory directory = new Directory(this.tempDir);
		this.handler.handle(request, directory);
		File buildInfo = new File(directory.getFile(), "build-info.json");
		assertThat(buildInfo).exists().hasContent(BUILD_INFO_JSON);
	}

	private InRequest createRequest(boolean generateMavenMetadata, boolean saveBuildInfo, boolean downloadArtifacts) {
		return createRequest(generateMavenMetadata, saveBuildInfo, downloadArtifacts, true);
	}

	private InRequest createRequest(boolean generateMavenMetadata, boolean saveBuildInfo, boolean downloadArtifacts,
			boolean downloadChecksums) {
		return createRequest(generateMavenMetadata, saveBuildInfo, downloadArtifacts, downloadChecksums, 1);
	}

	private InRequest createRequest(boolean generateMavenMetadata, boolean saveBuildInfo, boolean downloadArtifacts,
			boolean downloadChecksums, int threads) {
		InRequest request = new InRequest(
				new Source("https://ci.example.com", "admin", "password", "my-build", null, null), new Version("1234"),
				new Params(false, generateMavenMetadata, saveBuildInfo, downloadArtifacts, downloadChecksums, threads));
		return request;
	}

}
