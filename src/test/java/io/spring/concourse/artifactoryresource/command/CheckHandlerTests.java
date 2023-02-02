/*
 * Copyright 2017-2023 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import io.spring.concourse.artifactoryresource.artifactory.Artifactory;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryBuildRuns;
import io.spring.concourse.artifactoryresource.artifactory.ArtifactoryServer;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildRun;
import io.spring.concourse.artifactoryresource.command.payload.CheckRequest;
import io.spring.concourse.artifactoryresource.command.payload.CheckResponse;
import io.spring.concourse.artifactoryresource.command.payload.Source;
import io.spring.concourse.artifactoryresource.command.payload.Version;
import io.spring.concourse.artifactoryresource.util.ArtifactoryDateFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link CheckHandler}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @author Gabriel Petrovay
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CheckHandlerTests {

	private static final Version VERSION1 = new Version("1", ArtifactoryDateFormat.parse("2014-01-20T12:01:02.003Z"));

	private static final Version VERSION2 = new Version("2", ArtifactoryDateFormat.parse("2014-01-21T12:01:02.003Z"));

	private static final Version VERSION3 = new Version("3", ArtifactoryDateFormat.parse("2014-01-22T12:01:02.003Z"));

	private static final Version VERSION4 = new Version("4", ArtifactoryDateFormat.parse("2014-01-23T12:01:02.003Z"));

	private static final BuildRun RUN1 = new BuildRun("1", VERSION1.getStarted());

	private static final BuildRun RUN2 = new BuildRun("2", VERSION2.getStarted());

	private static final BuildRun RUN3 = new BuildRun("3", VERSION3.getStarted());

	private static final BuildRun RUN4 = new BuildRun("4", VERSION4.getStarted());

	@Mock
	private Artifactory artifactory;

	@Mock
	private ArtifactoryServer artifactoryServer;

	@Mock
	private ArtifactoryBuildRuns artifactoryBuildRuns;

	private CheckHandler handler;

	@BeforeEach
	void setup() {
		given(this.artifactory.server("https://ci.example.com", "admin", "password", null))
				.willReturn(this.artifactoryServer);
		given(this.artifactoryServer.buildRuns(eq("my-build"), any())).willReturn(this.artifactoryBuildRuns);
		this.handler = new CheckHandler(this.artifactory);
	}

	@Test
	void handleWhenVersionIsMissingRespondsWithLatest() {
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2,
				CheckHandlerTests.RUN4, CheckHandlerTests.RUN1));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, null);
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION4);
	}

	@Test
	void handleWhenVersionIsPresentRespondsWithListOfVersions() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter(null, VERSION2.getStarted()))
				.willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2, CheckHandlerTests.RUN4));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, VERSION2);
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION4, VERSION3, VERSION2);
	}

	@Test
	void handleWhenVersionIsPresentAndLatestRespondsWithListOfVersions() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter(null, VERSION4.getStarted()))
				.willReturn(List.of(CheckHandlerTests.RUN4));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, VERSION4);
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION4);
	}

	@Test
	void handleWhenVersionIsPresentButRemovedFromArtifactoryRespondsWithLatest() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter(null, VERSION4.getStarted()))
				.willReturn(Collections.emptyList());
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(List.of(CheckHandlerTests.RUN3));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, VERSION4);
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION3);
	}

	@Test
	void handleWhenVersionIsPresentButAllHaveBeenRemovedFromArtifactoryReturnsEmptyList() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter(null, VERSION4.getStarted()))
				.willReturn(Collections.emptyList());
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(Collections.emptyList());
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, VERSION4);
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).isEmpty();
	}

	@Test
	void handleWhenLegacyVersionIsPresentRespondsWithListOfVersions() {
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2,
				CheckHandlerTests.RUN4, CheckHandlerTests.RUN1));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, new Version("2", null));
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION4, VERSION3, VERSION2);
	}

	@Test
	void handleWhenLegacyVersionIsPresentAndLatestRespondsWithListOfVersions() {
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2,
				CheckHandlerTests.RUN4, CheckHandlerTests.RUN1));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, new Version("4", null));
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION4);
	}

	@Test
	void handleWhenLegacyVersionIsPresentButRemovedFromArtifactoryRespondsWithLatest() {
		given(this.artifactoryBuildRuns.getAll(null))
				.willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2, CheckHandlerTests.RUN1));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, new Version("4", null));
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).containsExactly(VERSION3);
	}

	@Test
	void handleWhenLegacyVersionIsPresentButAllHaveBeenRemovedFromArtifactoryReturnsEmptyList() {
		given(this.artifactoryBuildRuns.getAll(null)).willReturn(Collections.emptyList());
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build");
		CheckRequest request = new CheckRequest(source, new Version("4", null));
		CheckResponse response = this.handler.handle(request);
		assertThat(response.getVersions()).isEmpty();
	}

	@Test
	void handleWhenHasCheckLimitLimitsResults() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter(null, VERSION2.getStarted()))
				.willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2, CheckHandlerTests.RUN4));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build", null, 123, null, null);
		CheckRequest request = new CheckRequest(source, VERSION4);
		this.handler.handle(request);
		verify(this.artifactoryServer).buildRuns("my-build", 123);
	}

	@Test
	void handleWhenHasBuildPrefixLimitsResults() {
		given(this.artifactoryBuildRuns.getStartedOnOrAfter("main-", VERSION2.getStarted()))
				.willReturn(List.of(CheckHandlerTests.RUN3, CheckHandlerTests.RUN2, CheckHandlerTests.RUN4));
		Source source = new Source("https://ci.example.com", "admin", "password", "my-build", "main-", 123, null, null);
		CheckRequest request = new CheckRequest(source, VERSION4);
		this.handler.handle(request);
		verify(this.artifactoryServer).buildRuns("my-build", 123);
	}

}
