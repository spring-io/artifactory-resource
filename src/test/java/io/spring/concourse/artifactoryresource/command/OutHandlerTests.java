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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryBuildRuns;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryRepository;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.DeployOption;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest.ArtifactSet;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest.Params;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.io.DirectoryScanner;
import io.spring.concourse.artifactoryresource.io.FileSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link OutHandler}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OutHandlerTests {

	private static final byte[] NO_BYTES = {};

	@TempDir
	File tempDir;

	@Mock
	private Artifactory artifactory;

	@Mock
	private ArtifactoryServer artifactoryServer;

	@Mock
	private ArtifactoryRepository artifactoryRepository;

	@Mock
	private ArtifactoryBuildRuns artifactoryBuildRuns;

	@Mock
	private BuildNumberGenerator buildNumberGenerator;

	@Mock
	private DirectoryScanner directoryScanner;

	@Mock
	private ModuleLayouts moduleLayouts;

	private OutHandler handler;

	@Captor
	private ArgumentCaptor<DeployableArtifact> artifactCaptor;

	@Captor
	private ArgumentCaptor<DeployOption> optionsCaptor;

	@Captor
	private ArgumentCaptor<List<BuildModule>> modulesCaptor;

	@BeforeEach
	void setup() {
		given(this.artifactory.server("https://ci.example.com", "admin", "password", null))
				.willReturn(this.artifactoryServer);
		given(this.artifactoryServer.repository("libs-snapshot-local")).willReturn(this.artifactoryRepository);
		given(this.artifactoryServer.buildRuns("my-build")).willReturn(this.artifactoryBuildRuns);
		given(this.moduleLayouts.getBuildModulesGenerator(anyString())).willReturn(new MockBuildModulesGenerator());
		this.handler = new OutHandler(this.artifactory, this.buildNumberGenerator, this.moduleLayouts,
				this.directoryScanner);
	}

	@Test
	void handleWhenEmptyThrowsException() {
		OutRequest request = createRequest("1234");
		Directory directory = new Directory(this.tempDir);
		assertThatIllegalStateException().isThrownBy(() -> this.handler.handle(request, directory))
				.withMessage("No artifacts found in empty directory");
	}

	@Test
	void handleWhenNoFilesThrowsException() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		given(this.directoryScanner.scan(any(), any(), any())).willReturn(FileSet.of());
		assertThatIllegalStateException().isThrownBy(() -> this.handler.handle(request, directory))
				.withMessage("No artifacts found to deploy");
	}

	@Test
	void handleWhenBuildNumberIsMissingGeneratesBuildNumber() throws Exception {
		given(this.buildNumberGenerator.generateBuildNumber()).willReturn("2000");
		OutRequest request = createRequest(null);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("2000"), any(), any(), any());
	}

	@Test
	void handleWhenBuildNumberIsSpecifiedUsesBuildNumber() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"), any(), any(), any());
		verifyNoInteractions(this.buildNumberGenerator);
	}

	@Test
	void handleDeploysArtifacts() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture(), this.optionsCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getPath()).isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
		assertThat(deployed.getProperties()).containsEntry("build.name", "my-build")
				.containsEntry("build.number", "1234").containsKey("build.timestamp");
		assertThat(this.optionsCaptor.getAllValues()).isEmpty();
	}

	@Test
	void handleDeploysArtifactsInBatches() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		List<File> files = new ArrayList<>();
		Directory foos = createStructure(directory, "folder", "com", "example", "foo", "0.0.1");
		Directory bars = createStructure(directory, "folder", "com", "example", "bar", "0.0.1");
		Directory bazs = createStructure(directory, "folder", "com", "example", "baz", "0.0.1");
		files.add(new File(foos.getFile(), "foo-0.0.1.jar"));
		files.add(new File(bars.getFile(), "bar-0.0.1.jar"));
		files.add(new File(bazs.getFile(), "baz-0.0.1.jar"));
		files.add(new File(foos.getFile(), "foo-0.0.1.pom"));
		files.add(new File(bars.getFile(), "bar-0.0.1.pom"));
		files.add(new File(bazs.getFile(), "baz-0.0.1.pom"));
		files.add(new File(foos.getFile(), "foo-0.0.1-javadoc.jar"));
		files.add(new File(bars.getFile(), "bar-0.0.1-javadoc.jar"));
		files.add(new File(bazs.getFile(), "baz-0.0.1-javadoc.jar"));
		files.add(new File(foos.getFile(), "foo-0.0.1-sources.jar"));
		files.add(new File(bars.getFile(), "bar-0.0.1-sources.jar"));
		files.add(new File(bazs.getFile(), "baz-0.0.1-sources.jar"));
		createEmptyFiles(files);
		given(this.directoryScanner.scan(any(), any(), any())).willReturn(FileSet.of(files));
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository, times(12)).deploy(this.artifactCaptor.capture(),
				this.optionsCaptor.capture());
		List<DeployableArtifact> values = this.artifactCaptor.getAllValues();
		for (int i = 0; i < 3; i++) {
			assertThat(values.get(i).getPath()).doesNotContain("javadoc", "sources").endsWith(".jar");
		}
		for (int i = 3; i < 6; i++) {
			assertThat(values.get(i).getPath()).endsWith(".pom");
		}
		for (int i = 6; i < 12; i++) {
			assertThat(values.get(i).getPath())
					.matches((path) -> path.endsWith("-javadoc.jar") || path.endsWith("-sources.jar"));
		}
	}

	@Test
	void handleWhenHasArtifactSetDeploysWithAdditionalProperties() throws Exception {
		List<ArtifactSet> artifactSet = new ArrayList<>();
		List<String> include = Collections.singletonList("/**/foo-0.0.1.jar");
		List<String> exclude = null;
		Map<String, String> properties = Collections.singletonMap("foo", "bar");
		artifactSet.add(new ArtifactSet(include, exclude, properties));
		OutRequest request = createRequest("1234", null, null, false, false, artifactSet, 1);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getProperties()).containsEntry("foo", "bar");
	}

	@Test
	void handleAddsBuildRun() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"), eq("https://ci.example.com/1234"), any(),
				this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		assertThat(buildModules).hasSize(1);
		BuildModule buildModule = buildModules.get(0);
		assertThat(buildModule.getArtifacts()).hasSize(1);
		assertThat(buildModule.getArtifacts().get(0).getName()).isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
		assertThat(buildModule.getId()).isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
	}

	@Test
	void handleUsesIncludesAndExcludes() throws Exception {
		List<String> include = Arrays.asList("foo");
		List<String> exclude = Arrays.asList("bar");
		OutRequest request = createRequest("1234", include, exclude);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.directoryScanner).scan(any(), eq(include), eq(exclude));
	}

	@Test
	void handleFiltersChecksumFiles() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		List<File> checksumFiles = new ArrayList<>();
		checksumFiles.add(new File(directory.getSubDirectory("folder").getFile(), "foo.jar.md5"));
		checksumFiles.add(new File(directory.getSubDirectory("folder").getFile(), "foo.jar.sha1"));
		checksumFiles.add(new File(directory.getSubDirectory("folder").getFile(), "foo.jar.sha256"));
		checksumFiles.add(new File(directory.getSubDirectory("folder").getFile(), "foo.jar.sha512"));
		configureMockScanner(directory, checksumFiles);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"), eq("https://ci.example.com/1234"), any(),
				this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		assertThat(buildModules).hasSize(1);
		BuildModule buildModule = buildModules.get(0);
		assertThat(buildModule.getArtifacts()).hasSize(1);
	}

	@Test
	void handleWhenStripSnapshotTimestampsIsFalseDoesNotFilterMetadataFiles() throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestampMetadata(false, "maven-metadata.xml");
		assertThat(buildModules).hasSize(2);
	}

	@Test
	void handleWhenStripSnapshotTimestampsIsTrueFiltersMetadataFiles() throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestampMetadata(true, "maven-metadata.xml");
		assertThat(buildModules).hasSize(1);
	}

	@Test
	void handleWhenStripSnapshotTimestampsIsFalseDoesNotFilterLocalMetadataFiles() throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestampMetadata(false, "maven-metadata-local.xml");
		assertThat(buildModules).hasSize(2);
	}

	@Test
	void handleWhenStripSnapshotTimestampsIsTrueFiltersLocalMetadataFiles() throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestampMetadata(true, "maven-metadata-local.xml");
		assertThat(buildModules).hasSize(1);
	}

	private List<BuildModule> testStripSnapshotTimestampMetadata(boolean stripSnapshotTimestamps, String metadataFile)
			throws IOException {
		OutRequest request = createRequest("1234", null, null, stripSnapshotTimestamps, false, null, 1);
		Directory directory = createDirectory();
		List<File> metadataFiles = new ArrayList<>();
		metadataFiles.add(new File(directory.getSubDirectory("folder").getFile(), metadataFile));
		configureMockScanner(directory, metadataFiles);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"), eq("https://ci.example.com/1234"), any(),
				this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		return buildModules;
	}

	@Test
	void handleWhenStripSnapshotTimestampsChangesDeployArtifactPath() throws Exception {
		OutRequest request = createRequest("1234", null, null, true, false, null, 1);
		Directory directory = createDirectory();
		configureMockScanner(directory, Collections.emptyList(), "1.0.0.BUILD-SNAPSHOT",
				"1.0.0.BUILD-20171005.194031-1");
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getPath()).isEqualTo("/com/example/foo/1.0.0.BUILD-SNAPSHOT/foo-1.0.0.BUILD-SNAPSHOT.jar");
		assertThat(deployed.getProperties()).containsEntry("build.name", "my-build")
				.containsEntry("build.number", "1234").containsKey("build.timestamp");
	}

	@Test
	void handleWhenDisableChecksumUploadDoesNotUseChecksumUpload() throws Exception {
		OutRequest request = createRequest("1234", null, null, false, true, null, 1);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture(), this.optionsCaptor.capture());
		assertThat(this.optionsCaptor.getAllValues()).containsOnly(DeployOption.DISABLE_CHECKSUM_UPLOADS);
	}

	private OutRequest createRequest(String buildNumber) {
		return createRequest(buildNumber, null, null);
	}

	private OutRequest createRequest(String buildNumber, List<String> include, List<String> exclude) {
		return createRequest(buildNumber, include, exclude, false, false, null, 1);
	}

	private OutRequest createRequest(String buildNumber, List<String> include, List<String> exclude,
			boolean stripSnapshotTimestamps, boolean disableChecksumUploads, List<ArtifactSet> artifactSet,
			int threads) {
		return new OutRequest(new Source("https://ci.example.com", "admin", "password", "my-build", null, null),
				new Params(false, "libs-snapshot-local", buildNumber, "folder", include, exclude, "mock",
						"https://ci.example.com/1234", stripSnapshotTimestamps, disableChecksumUploads, artifactSet,
						threads));
	}

	private Directory createDirectory() throws IOException {
		new File(this.tempDir, "folder").mkdirs();
		return new Directory(this.tempDir);
	}

	private void configureMockScanner(Directory directory) throws IOException {
		configureMockScanner(directory, Collections.emptyList());
	}

	private void configureMockScanner(Directory directory, List<File> extraFiles) throws IOException {
		configureMockScanner(directory, extraFiles, "0.0.1", "0.0.1");
	}

	private void configureMockScanner(Directory directory, List<File> extraFiles, String version,
			String snapshotVersion) throws IOException {
		directory = createStructure(directory, "folder", "com", "example", "foo", version);
		List<File> files = new ArrayList<>();
		File file = new File(directory.getFile(), "foo-" + snapshotVersion + ".jar");
		files.add(file);
		files.addAll(extraFiles);
		createEmptyFiles(files);
		given(this.directoryScanner.scan(any(), any(), any())).willReturn(FileSet.of(files));
	}

	private Directory createStructure(Directory directory, String... paths) {
		for (String path : paths) {
			new File(directory.getFile(), path).mkdirs();
			directory = directory.getSubDirectory(path);
		}
		return directory;
	}

	private void createEmptyFiles(List<File> files) throws IOException {
		for (File file : files) {
			FileCopyUtils.copy(NO_BYTES, file);
		}
	}

}
