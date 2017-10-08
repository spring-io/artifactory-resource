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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for {@link OutHandler}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class OutHandlerTests {

	private static final byte[] NO_BYTES = {};

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		given(this.artifactory.server("http://ci.example.com", "admin", "password"))
				.willReturn(this.artifactoryServer);
		given(this.artifactoryServer.repository("libs-snapshot-local"))
				.willReturn(this.artifactoryRepository);
		given(this.artifactoryServer.buildRuns("my-build"))
				.willReturn(this.artifactoryBuildRuns);
		given(this.moduleLayouts.getBuildModulesGenerator(anyString()))
				.willReturn(new MockBuildModulesGenerator());
		this.handler = new OutHandler(this.artifactory, this.buildNumberGenerator,
				this.moduleLayouts, this.directoryScanner);
	}

	@Test
	public void handleWhenNoFilesShouldThrowException() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("No artifacts found to deploy");
		this.handler.handle(request, directory);
	}

	@Test
	public void handleWhenBuildNumberIsMissingShouldGenerateBuildNumber()
			throws Exception {
		given(this.buildNumberGenerator.generateBuildNumber()).willReturn("2000");
		OutRequest request = createRequest(null);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("2000"), any(), any());
	}

	@Test
	public void handleWhenBuildNumberIsSpecifiedShouldUseBuildNumber() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"), any(), any());
		verifyZeroInteractions(this.buildNumberGenerator);
	}

	@Test
	public void handleShouldDeployArtifacts() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture(),
				this.optionsCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getPath()).isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
		assertThat(deployed.getProperties()).containsEntry("build.name", "my-build")
				.containsEntry("build.number", "1234");
		assertThat(this.optionsCaptor.getAllValues()).isEmpty();
	}

	@Test
	public void handleWhenHasArtifactSetShouldDeployWithAdditionalProperties()
			throws Exception {
		List<ArtifactSet> artifactSet = new ArrayList<>();
		List<String> include = Collections.singletonList("/**/foo-0.0.1.jar");
		List<String> exclude = null;
		Map<String, String> properties = Collections.singletonMap("foo", "bar");
		artifactSet.add(new ArtifactSet(include, exclude, properties));
		OutRequest request = createRequest("1234", null, null, false, false, artifactSet);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getProperties()).containsEntry("foo", "bar");
	}

	@Test
	public void handleShouldAddBuildRun() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"),
				eq("http://ci.example.com/1234"), this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		assertThat(buildModules).hasSize(1);
		BuildModule buildModule = buildModules.get(0);
		assertThat(buildModule.getArtifacts()).hasSize(1);
		assertThat(buildModule.getArtifacts().get(0).getName())
				.isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
		assertThat(buildModule.getId()).isEqualTo("/com/example/foo/0.0.1/foo-0.0.1.jar");
	}

	@Test
	public void handleShouldUseIncludesAndExcludes() throws Exception {
		List<String> include = Arrays.asList("foo");
		List<String> exclude = Arrays.asList("bar");
		OutRequest request = createRequest("1234", include, exclude);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.directoryScanner).scan(any(), eq(include), eq(exclude));
	}

	@Test
	public void handleShouldFilterChecksumFiles() throws Exception {
		OutRequest request = createRequest("1234");
		Directory directory = createDirectory();
		List<File> checksumFiles = new ArrayList<>();
		checksumFiles.add(
				new File(directory.getSubDirectory("folder").getFile(), "foo.jar.md5"));
		checksumFiles.add(
				new File(directory.getSubDirectory("folder").getFile(), "foo.jar.sha1"));
		configureMockScanner(directory, checksumFiles);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"),
				eq("http://ci.example.com/1234"), this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		assertThat(buildModules).hasSize(1);
		BuildModule buildModule = buildModules.get(0);
		assertThat(buildModule.getArtifacts()).hasSize(1);
	}

	@Test
	public void handleWhenStripSnapshotTimestampsIsFalseShouldNotFilterMetadataFiles()
			throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestamptsMetadata(false);
		assertThat(buildModules).hasSize(2);
	}

	@Test
	public void handleWhenStripSnapshotTimestampsIsTrueShouldFilterMetadataFiles()
			throws Exception {
		List<BuildModule> buildModules = testStripSnapshotTimestamptsMetadata(true);
		assertThat(buildModules).hasSize(1);
	}

	private List<BuildModule> testStripSnapshotTimestamptsMetadata(
			boolean stripSnapshotTimestamps) throws IOException {
		OutRequest request = createRequest("1234", null, null, stripSnapshotTimestamps,
				false, null);
		Directory directory = createDirectory();
		List<File> metadataFiles = new ArrayList<>();
		metadataFiles.add(new File(directory.getSubDirectory("folder").getFile(),
				"maven-metadata.xml"));
		configureMockScanner(directory, metadataFiles);
		this.handler.handle(request, directory);
		verify(this.artifactoryBuildRuns).add(eq("1234"),
				eq("http://ci.example.com/1234"), this.modulesCaptor.capture());
		List<BuildModule> buildModules = this.modulesCaptor.getValue();
		return buildModules;
	}

	@Test
	public void handleWhenStripSnapshotTimestampsShouldChangeDeploArtifactPath()
			throws Exception {
		OutRequest request = createRequest("1234", null, null, true, false, null);
		Directory directory = createDirectory();
		configureMockScanner(directory, Collections.emptyList(), "1.0.0.BUILD-SNAPSHOT",
				"1.0.0.BUILD-20171005.194031-1");
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture());
		DeployableArtifact deployed = this.artifactCaptor.getValue();
		assertThat(deployed.getPath()).isEqualTo(
				"/com/example/foo/1.0.0.BUILD-SNAPSHOT/foo-1.0.0.BUILD-SNAPSHOT.jar");
		assertThat(deployed.getProperties()).containsEntry("build.name", "my-build")
				.containsEntry("build.number", "1234");
	}

	@Test
	public void handleWhenDisableChecksumUploadShouldNotUseChecksumUpload()
			throws Exception {
		OutRequest request = createRequest("1234", null, null, false, true, null);
		Directory directory = createDirectory();
		configureMockScanner(directory);
		this.handler.handle(request, directory);
		verify(this.artifactoryRepository).deploy(this.artifactCaptor.capture(),
				this.optionsCaptor.capture());
		assertThat(this.optionsCaptor.getAllValues())
				.containsOnly(DeployOption.DISABLE_CHECKSUM_UPLOADS);
	}

	private OutRequest createRequest(String buildNumber) {
		return createRequest(buildNumber, null, null);
	}

	private OutRequest createRequest(String buildNumber, List<String> include,
			List<String> exclude) {
		return createRequest(buildNumber, include, exclude, false, false, null);
	}

	private OutRequest createRequest(String buildNumber, List<String> include,
			List<String> exclude, boolean stripSnapshotTimestamps,
			boolean disableChecksumUploads, List<ArtifactSet> artifactSet) {
		return new OutRequest(
				new Source("http://ci.example.com", "admin", "password", "my-build"),
				new Params(false, buildNumber, "libs-snapshot-local", "folder", include,
						exclude, "mock", "http://ci.example.com/1234",
						stripSnapshotTimestamps, disableChecksumUploads, artifactSet));
	}

	private Directory createDirectory() throws IOException {
		File sources = this.temporaryFolder.newFolder();
		new File(sources, "folder").mkdirs();
		Directory directory = new Directory(sources);
		return directory;
	}

	private void configureMockScanner(Directory directory) throws IOException {
		configureMockScanner(directory, Collections.emptyList());
	}

	private void configureMockScanner(Directory directory, List<File> extraFiles)
			throws IOException {
		configureMockScanner(directory, extraFiles, "0.0.1", "0.0.1");
	}

	private void configureMockScanner(Directory directory, List<File> extraFiles,
			String version, String snapshotVersion) throws IOException {
		directory = createStructure(directory, "folder", "com", "example", "foo",
				version);
		List<File> files = new ArrayList<>();
		File file = new File(directory.getFile(), "foo-" + snapshotVersion + ".jar");
		files.add(file);
		files.addAll(extraFiles);
		createEmptyFiles(files);
		given(this.directoryScanner.scan(any(), any(), any())).willReturn(files);
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
