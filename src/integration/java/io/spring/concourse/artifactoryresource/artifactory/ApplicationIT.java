/*
 * Copyright 2017-2020 the original author or authors.
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.artifactory.payload.ContinuousIntegrationAgent;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableFileArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployedArtifact;
import io.spring.concourse.artifactoryresource.command.BuildNumberGenerator;
import io.spring.concourse.artifactoryresource.io.Checksum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests against a real artifactory instance started using Docker Compose.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationIT {

	@RegisterExtension
	static ArtifactoryServerConnection connection = new ArtifactoryServerConnection();

	@TempDir
	File tempDir;

	@Autowired
	private Artifactory artifactory;

	@Test
	void integrationTest() throws Exception {
		String buildNumber = generateBuildNumber();
		File file = createLargeFile();
		ArtifactoryRepository repository = getServer().repository("example-repo-local");
		ArtifactoryBuildRuns buildRuns = getServer().buildRuns("my-build");
		Date buildTimestamp = new Date();
		deployArtifact(repository, buildNumber, buildTimestamp, file);
		addBuildRun(buildRuns, buildNumber, buildTimestamp);
		getBuildRuns(buildRuns, buildNumber);
		downloadUsingBuildRun(repository, buildRuns, buildNumber, file);
	}

	private String generateBuildNumber() {
		return new BuildNumberGenerator().generateBuildNumber();
	}

	private void deployArtifact(ArtifactoryRepository artifactoryRepository, String buildNumber, Date buildTimestamp,
			File file) throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put("build.name", "my-build");
		properties.put("build.number", buildNumber);
		properties.put("build.timestamp", Long.toString(buildTimestamp.toInstant().toEpochMilli()));
		DeployableArtifact artifact = new DeployableFileArtifact("/foo/bar", file, properties, null);
		artifactoryRepository.deploy(artifact);
	}

	private File createLargeFile() throws IOException, FileNotFoundException {
		File file = new File(this.tempDir, "temp");
		FileOutputStream stream = new FileOutputStream(file);
		int size = 0;
		Random random = new Random();
		byte[] bytes = new byte[1024];
		while (size < 10 * 1024 * 1024) {
			random.nextBytes(bytes);
			StreamUtils.copy(bytes, stream);
			size += bytes.length;
		}
		stream.close();
		return file;
	}

	private void addBuildRun(ArtifactoryBuildRuns artifactoryBuildRuns, String buildNumber, Date buildTimestamp)
			throws Exception {
		BuildArtifact artifact = new BuildArtifact("test", "my-sha", "my-md5", "bar");
		BuildModule modules = new BuildModule("foo-test", Collections.singletonList(artifact));
		artifactoryBuildRuns.add(buildNumber, "ci.example.com", buildTimestamp,
				new ContinuousIntegrationAgent("Concourse", null), Collections.singletonList(modules));
	}

	private void getBuildRuns(ArtifactoryBuildRuns artifactoryBuildRuns, String buildNumber) {
		List<BuildRun> runs = artifactoryBuildRuns.getAll();
		assertThat(runs.get(0).getBuildNumber()).isEqualTo(buildNumber);
	}

	private void downloadUsingBuildRun(ArtifactoryRepository artifactoryRepository,
			ArtifactoryBuildRuns artifactoryBuildRuns, String buildNumber, File expectedContent) throws Exception {
		List<DeployedArtifact> results = artifactoryBuildRuns.getDeployedArtifacts(buildNumber);
		File folder = this.tempDir;
		for (DeployedArtifact result : results) {
			artifactoryRepository.download(result, folder, true);
		}
		assertThat(new File(folder, "foo/bar")).hasSameClassAs(expectedContent);
		Map<Checksum, String> checksums = Checksum.calculateAll(new FileSystemResource(expectedContent));
		assertThat(new File(folder, "foo/bar.md5")).hasContent(checksums.get(Checksum.MD5));
		assertThat(new File(folder, "foo/bar.sha1")).hasContent(checksums.get(Checksum.SHA1));
	}

	private ArtifactoryServer getServer() {
		return connection.getArtifactoryServer(this.artifactory);
	}

}
