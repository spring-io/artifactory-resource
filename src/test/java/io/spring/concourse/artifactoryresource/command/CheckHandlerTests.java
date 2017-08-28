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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryBuildRuns;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.command.payload.CheckRequest;
import io.spring.concourse.artifactoryresource.command.payload.CheckResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;
import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link CheckHandler}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
public class CheckHandlerTests {

	@Mock
	private Artifactory artifactory;

	@Mock
	private ArtifactoryServer artifactoryServer;

	@Mock
	private ArtifactoryBuildRuns artifactoryBuildRuns;

	private CheckHandler handler;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		List<BuildRun> runs = createBuildRuns();
		given(this.artifactory.server("http://ci.example.com", "admin", "password"))
				.willReturn(this.artifactoryServer);
		given(this.artifactoryServer.buildRuns("my-build"))
				.willReturn(this.artifactoryBuildRuns);
		given(this.artifactoryBuildRuns.getAll()).willReturn(runs);
		this.handler = new CheckHandler(this.artifactory);
	}

	private List<BuildRun> createBuildRuns() {
		// The API seems to return things in no specific order
		List<BuildRun> runs = new ArrayList<>();
		runs.add(new BuildRun("/2",
				ArtifactoryDateFormat.parse("2014-01-21T12:01:02.003+0000")));
		runs.add(new BuildRun("/1",
				ArtifactoryDateFormat.parse("2014-01-20T12:01:02.003+0000")));
		runs.add(new BuildRun("/4",
				ArtifactoryDateFormat.parse("2014-01-23T12:01:02.003+0000")));
		runs.add(new BuildRun("/3",
				ArtifactoryDateFormat.parse("2014-01-22T12:01:02.003+0000")));
		return runs;
	}

	@Test
	public void handleWhenVersionIsMissingShouldRespondWithLatest() throws Exception {
		CheckRequest request = new CheckRequest(
				new Source("http://ci.example.com", "admin", "password", "my-build"),
				null);
		CheckResponse response = this.handler.handle(request);
		Stream<String> buildsNumbers = response.getVersions().stream()
				.map(Version::getBuildNumber);
		assertThat(buildsNumbers.toArray()).containsExactly("4");
	}

	@Test
	public void handleWhenVersionIsPresentShouldRespondWithListOfVersions()
			throws Exception {
		CheckRequest request = new CheckRequest(
				new Source("http://ci.example.com", "admin", "password", "my-build"),
				new Version("2"));
		CheckResponse response = this.handler.handle(request);
		Stream<String> buildsNumbers = response.getVersions().stream()
				.map(Version::getBuildNumber);
		assertThat(buildsNumbers.toArray()).containsExactly("2", "3", "4");
	}

	@Test
	public void handleWhenVersionIsPresentAndLatestShouldRespondWithListOfVersions()
			throws Exception {
		CheckRequest request = new CheckRequest(
				new Source("http://ci.example.com", "admin", "password", "my-build"),
				new Version("4"));
		CheckResponse response = this.handler.handle(request);
		Stream<String> buildsNumbers = response.getVersions().stream()
				.map(Version::getBuildNumber);
		assertThat(buildsNumbers.toArray()).containsExactly("4");
	}

	@Test
	public void handleWhenNoVersionsFoundShouldRespondWithLatest() throws Exception {
		CheckRequest request = new CheckRequest(
				new Source("http://ci.example.com", "admin", "password", "my-build"),
				new Version("5"));
		CheckResponse response = this.handler.handle(request);
		Stream<String> buildsNumbers = response.getVersions().stream()
				.map(Version::getBuildNumber);
		assertThat(buildsNumbers.toArray()).containsExactly("4");
	}

}
