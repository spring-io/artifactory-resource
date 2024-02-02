/*
 * Copyright 2017-2024 the original author or authors.
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

import java.io.File;

import io.spring.concourse.artifactoryresource.command.payload.InRequest;
import io.spring.concourse.artifactoryresource.command.payload.InResponse;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.system.SystemInput;
import io.spring.concourse.artifactoryresource.system.SystemOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link InCommand}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@ExtendWith(MockitoExtension.class)
class InCommandTests {

	@TempDir
	File tempDir;

	@Mock
	private SystemInput systemInput;

	@Mock
	private SystemOutput systemOutput;

	@Mock
	private InHandler handler;

	@Captor
	private ArgumentCaptor<Directory> directoryCaptor;

	private InCommand command;

	@BeforeEach
	void setup() {
		this.command = new InCommand(this.systemInput, this.systemOutput, this.handler);
	}

	@Test
	void runCallsHandler() throws Exception {
		InRequest request = mock(InRequest.class);
		InResponse response = mock(InResponse.class);
		given(this.systemInput.read(InRequest.class)).willReturn(request);
		given(this.handler.handle(eq(request), any())).willReturn(response);
		String dir = StringUtils.cleanPath(this.tempDir.getPath());
		this.command.run(new DefaultApplicationArguments(new String[] { "in", dir }));
		verify(this.handler).handle(eq(request), this.directoryCaptor.capture());
		verify(this.systemOutput).write(response);
		assertThat(this.directoryCaptor.getValue().getFile()).isEqualTo(this.tempDir);
	}

	@Test
	void runWhenFolderArgIsMissingThrowsException() throws Exception {
		InRequest request = mock(InRequest.class);
		given(this.systemInput.read(InRequest.class)).willReturn(request);
		assertThatIllegalStateException()
			.isThrownBy(() -> this.command.run(new DefaultApplicationArguments(new String[] {})))
			.withMessage("No directory argument specified");
	}

}
