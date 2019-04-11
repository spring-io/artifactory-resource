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

package io.spring.concourse.artifactoryresource.artifactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableByteArrayArtifact;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Tests for {@link HttpArtifactoryRepository}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@RunWith(SpringRunner.class)
@RestClientTest(HttpArtifactory.class)
public class HttpArtifactoryRepositoryTests {

	private static final byte[] BYTES;

	static {
		BYTES = new byte[1024 * 11];
		new Random().nextBytes(BYTES);
	}

	@Autowired
	private MockRestServiceServer server;

	@Autowired
	private MockServerRestTemplateCustomizer customizer;

	@Autowired
	private Artifactory artifactory;

	private ArtifactoryRepository artifactoryRepository;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setup() {
		this.artifactoryRepository = this.artifactory
				.server("https://repo.example.com", "admin", "password")
				.repository("libs-snapshot-local");
	}

	@After
	public void tearDown() throws Exception {
		this.customizer.getExpectationManagers().clear();
	}

	@Test
	public void deployUploadsTheDeployableArtifact() throws IOException {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				BYTES);
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
				.andExpect(header("X-Checksum-Deploy", "true"))
				.andExpect(header("X-Checksum-Sha1", artifact.getChecksums().getSha1()))
				.andExpect(header("content-length", String.valueOf(artifact.getSize())))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
		this.server.expect(requestTo(url)).andRespond(withSuccess());
		this.artifactoryRepository.deploy(artifact);
		this.server.verify();
	}

	@Test
	public void deployUploadsTheDeployableArtifactWithMatrixParameters() {
		Map<String, String> properties = new HashMap<>();
		properties.put("buildNumber", "1");
		properties.put("revision", "123");
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				BYTES, properties);
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar;buildNumber=1;revision=123";
		this.server.expect(requestTo(url)).andRespond(withSuccess());
		this.artifactoryRepository.deploy(artifact);
		this.server.verify();
	}

	@Test
	public void deployWhenChecksumMatchesDoesNotUpload() throws Exception {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				BYTES);
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
				.andExpect(header("X-Checksum-Deploy", "true"))
				.andExpect(header("X-Checksum-Sha1", artifact.getChecksums().getSha1()))
				.andRespond(withSuccess());
		this.artifactoryRepository.deploy(artifact);
		this.server.verify();
	}

	@Test
	public void deployWhenChecksumUploadFailsWithHttpClientErrorExceptionThrows()
			throws Exception {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				BYTES);
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
				.andExpect(header("X-Checksum-Deploy", "true"))
				.andExpect(header("X-Checksum-Sha1", artifact.getChecksums().getSha1()))
				.andRespond(withStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE));
		assertThatExceptionOfType(HttpClientErrorException.class)
				.isThrownBy(() -> this.artifactoryRepository.deploy(artifact));
	}

	@Test
	public void deployWhenSmallFileDoesNotUseChecksum() throws Exception {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				"foo".getBytes());
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
				.andExpect(noChecksumHeader()).andRespond(withSuccess());
		this.artifactoryRepository.deploy(artifact);
		this.server.verify();
	}

	@Test
	public void deployWhenNoChecksumUploadOptionFileDoesNotUseChecksum()
			throws Exception {
		DeployableArtifact artifact = new DeployableByteArrayArtifact("/foo/bar.jar",
				BYTES);
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.PUT))
				.andExpect(noChecksumHeader()).andRespond(withSuccess());
		this.artifactoryRepository.deploy(artifact,
				DeployOption.DISABLE_CHECKSUM_UPLOADS);
		this.server.verify();
	}

	private RequestMatcher noChecksumHeader() {
		return (request) -> assertThat(request.getHeaders().keySet())
				.doesNotContain("X-Checksum-Deploy");
	}

	@Test
	public void downloadFetchsArtifactAndWriteToFile() throws Exception {
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		expectFileDownload(url);
		File destination = this.temporaryFolder.newFolder();
		this.artifactoryRepository.download("foo/bar.jar", destination, false);
		assertThat(new File(new File(destination, "foo"), "bar.jar")).exists().isFile();
		this.server.verify();
	}

	@Test
	public void downloadFetchsChecksumFiles() throws Exception {
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		expectFileDownload(url);
		expectFileDownload(url + ".md5");
		expectFileDownload(url + ".sha1");
		File destination = this.temporaryFolder.newFolder();
		this.artifactoryRepository.download("foo/bar.jar", destination, true);
		File folder = new File(destination, "foo");
		assertThat(new File(folder, "bar.jar")).exists().isFile();
		assertThat(new File(folder, "bar.jar.md5")).exists().isFile();
		assertThat(new File(folder, "bar.jar.sha1")).exists().isFile();
		this.server.verify();
	}

	@Test
	public void downloadWhenChecksumFileDoesNotFetchChecksumFiles() throws Exception {
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar.md5";
		expectFileDownload(url);
		File destination = this.temporaryFolder.newFolder();
		this.artifactoryRepository.download("foo/bar.jar.md5", destination, true);
		File folder = new File(destination, "foo");
		assertThat(new File(folder, "bar.jar.md5")).exists().isFile();
		assertThat(new File(folder, "bar.jar.md5.md5")).doesNotExist();
		assertThat(new File(folder, "bar.jar.md5.sha1")).doesNotExist();
		this.server.verify();
	}

	@Test
	public void downloadIgnoresChecksumFileFailures() throws Exception {
		String url = "https://repo.example.com/libs-snapshot-local/foo/bar.jar";
		expectFileDownload(url);
		expectFile404(url + ".md5");
		expectFile404(url + ".sha1");
		File destination = this.temporaryFolder.newFolder();
		this.artifactoryRepository.download("foo/bar.jar", destination, true);
		File folder = new File(destination, "foo");
		assertThat(new File(folder, "bar.jar")).exists().isFile();
		assertThat(new File(folder, "bar.jar.md5")).doesNotExist();
		assertThat(new File(folder, "bar.jar.sha1")).doesNotExist();
		this.server.verify();
	}

	private void expectFileDownload(String url) {
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(new ByteArrayResource(new byte[] {}),
						MediaType.APPLICATION_OCTET_STREAM));
	}

	private void expectFile404(String url) {
		this.server.expect(requestTo(url)).andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));
	}

}
