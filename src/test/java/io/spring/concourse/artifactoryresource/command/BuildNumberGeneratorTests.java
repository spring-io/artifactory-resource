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

package io.spring.concourse.artifactoryresource.command;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuildNumberGenerator}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class BuildNumberGeneratorTests {

	@Test
	public void generateBuildNumberUsesFormattedInstant() throws Exception {
		Instant fixedInstant = LocalDateTime
				.parse("2011-12-03T10:15:30+00:00", DateTimeFormatter.ISO_DATE_TIME)
				.toInstant(ZoneOffset.UTC);
		Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
		String buildNumber = new BuildNumberGenerator(clock).generateBuildNumber();
		assertThat(buildNumber).isEqualTo("20111203101530000000000");
	}

	@Test
	public void generateBuildNumberWorksWithRealClock() throws Exception {
		String buildNumber = new BuildNumberGenerator().generateBuildNumber();
		assertThat(buildNumber).isNotNull().hasSize(23);
	}

}
