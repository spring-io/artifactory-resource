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

import java.io.File;

import io.spring.concourse.artifactoryresource.command.payload.InRequest;
import io.spring.concourse.artifactoryresource.command.payload.OutRequest;
import io.spring.concourse.artifactoryresource.command.payload.OutResponse;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.system.SystemInput;
import io.spring.concourse.artifactoryresource.system.SystemOutput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link OutCommand}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RunWith(SpringRunner.class)
public class OutCommandTests {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Mock
	private SystemInput systemInput;

	@Mock
	private SystemOutput systemOutput;

	@Mock
	private OutHandler handler;

	@Captor
	private ArgumentCaptor<Directory> directoryCaptor;

	private OutCommand command;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.command = new OutCommand(this.systemInput, this.systemOutput, this.handler);
	}

	@Test
	public void runCallsHandler() throws Exception {
		OutRequest request = mock(OutRequest.class);
		OutResponse response = mock(OutResponse.class);
		given(this.systemInput.read(OutRequest.class)).willReturn(request);
		given(this.handler.handle(eq(request), any())).willReturn(response);
		File tempFolder = this.temporaryFolder.newFolder();
		String dir = StringUtils.cleanPath(tempFolder.getPath());
		this.command.run(new DefaultApplicationArguments(new String[] { "out", dir }));
		verify(this.handler).handle(eq(request), this.directoryCaptor.capture());
		verify(this.systemOutput).write(response);
		assertThat(this.directoryCaptor.getValue().getFile()).isEqualTo(tempFolder);
	}

	@Test
	public void runWhenFolderArgIsMissingThrowsException() throws Exception {
		InRequest request = mock(InRequest.class);
		given(this.systemInput.read(InRequest.class)).willReturn(request);
		assertThatIllegalStateException().isThrownBy(
				() -> this.command.run(new DefaultApplicationArguments(new String[] {})))
				.withMessage("No directory argument specified");
	}

}
