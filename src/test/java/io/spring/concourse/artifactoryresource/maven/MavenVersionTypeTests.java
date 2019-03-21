/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.maven;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MavenVersionType}.
 *
 * @author Phillip Webb
 */
public class MavenVersionTypeTests {

	@Test
	public void fromVersionWhenTimestampReturnsTimestampSnapshot() throws Exception {
		assertThat(MavenVersionType.fromVersion("0.0.1.BUILD-20171005.194031-1"))
				.isEqualTo(MavenVersionType.TIMESTAMP_SNAPSHOT);
		assertThat(MavenVersionType.fromVersion("0.0.1-20171005.194031-1"))
				.isEqualTo(MavenVersionType.TIMESTAMP_SNAPSHOT);
	}

	@Test
	public void fromVersionWhenSnapshotReturnsSnapshot() throws Exception {
		assertThat(MavenVersionType.fromVersion("0.0.1.BUILD-SNAPSHOT"))
				.isEqualTo(MavenVersionType.SNAPSHOT);
		assertThat(MavenVersionType.fromVersion("0.0.1-SNAPSHOT"))
				.isEqualTo(MavenVersionType.SNAPSHOT);
	}

	@Test
	public void fromVersionWhenFixedReturnsFixed() throws Exception {
		assertThat(MavenVersionType.fromVersion("0.0.1.RELEASE"))
				.isEqualTo(MavenVersionType.FIXED);
		assertThat(MavenVersionType.fromVersion("0.0.1"))
				.isEqualTo(MavenVersionType.FIXED);

	}

}
