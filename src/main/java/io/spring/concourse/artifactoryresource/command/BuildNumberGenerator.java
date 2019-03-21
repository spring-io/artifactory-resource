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

package io.spring.concourse.artifactoryresource.command;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * Strategy used to generate build numbers when none is specified.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class BuildNumberGenerator {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter
			.ofPattern("yyyyMMddHHmmssnnnnnnnnn").withZone(ZoneOffset.UTC);

	private final Clock clock;

	public BuildNumberGenerator() {
		this(Clock.systemUTC());
	}

	BuildNumberGenerator(Clock clock) {
		this.clock = clock;
	}

	public String generateBuildNumber() {
		return FORMATTER.format(this.clock.instant());
	}

}
