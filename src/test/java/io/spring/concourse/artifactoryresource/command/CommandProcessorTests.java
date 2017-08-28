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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link CommandProcessor}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class CommandProcessorTests {

	private static final String[] NO_ARGS = {};

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void runWhenNoArgumentShouldThrowException() throws Exception {
		CommandProcessor processor = new CommandProcessor(
				Collections.singletonList(mock(Command.class)));
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("No command argument specified");
		processor.run(new DefaultApplicationArguments(NO_ARGS));
	}

	@Test
	public void runWhenUnknownCommandShouldThrowException() throws Exception {
		Command fooCommand = mock(Command.class);
		given(fooCommand.getName()).willReturn("foo");
		CommandProcessor processor = new CommandProcessor(
				Collections.singletonList(fooCommand));
		DefaultApplicationArguments args = new DefaultApplicationArguments(
				new String[] { "bar", "go" });
		this.thrown.expect(IllegalStateException.class);
		this.thrown.expectMessage("Unknown command 'bar'");
		processor.run(args);
	}

	@Test
	public void runShouldDelegateToCommand() throws Exception {
		Command fooCommand = mock(Command.class);
		given(fooCommand.getName()).willReturn("foo");
		Command barCommand = mock(Command.class);
		given(barCommand.getName()).willReturn("bar");
		CommandProcessor processor = new CommandProcessor(
				Arrays.asList(fooCommand, barCommand));
		DefaultApplicationArguments args = new DefaultApplicationArguments(
				new String[] { "bar", "go" });
		processor.run(args);
		verify(fooCommand, never()).run(any());
		verify(barCommand).run(args);
	}

}
