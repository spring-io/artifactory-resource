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

import io.spring.concourse.artifactoryresource.command.payload.CheckRequest;
import io.spring.concourse.artifactoryresource.command.payload.CheckResponse;
import io.spring.concourse.artifactoryresource.system.SystemInput;
import io.spring.concourse.artifactoryresource.system.SystemOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link CheckCommand}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@ExtendWith(MockitoExtension.class)
class CheckCommandTests {

	@Mock
	private SystemInput systemInput;

	@Mock
	private SystemOutput systemOutput;

	@Mock
	private CheckHandler handler;

	private CheckCommand command;

	@BeforeEach
	void setup() {
		this.command = new CheckCommand(this.systemInput, this.systemOutput, this.handler);
	}

	@Test
	void runCallsHandler() throws Exception {
		CheckRequest request = mock(CheckRequest.class);
		CheckResponse response = mock(CheckResponse.class);
		given(this.systemInput.read(CheckRequest.class)).willReturn(request);
		given(this.handler.handle(request)).willReturn(response);
		this.command.run(new DefaultApplicationArguments(new String[] {}));
		verify(this.handler).handle(request);
		verify(this.systemOutput).write(response);
	}

}
