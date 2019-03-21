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

package io.spring.concourse.artifactoryresource.system;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

/**
 * Write output to {@link SystemStreams#out()}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class SystemOutput {

	private final SystemStreams systemStreams;

	private final ObjectMapper objectMapper;

	public SystemOutput(SystemStreams systemStreams, ObjectMapper objectMapper) {
		this.systemStreams = systemStreams;
		this.objectMapper = objectMapper;
	}

	public <T> void write(T value) throws IOException {
		this.objectMapper.writeValue(this.systemStreams.out(), value);
	}

}
