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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SystemStreams}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class SystemStreamsTests {

	@Test
	public void reconfigureSystemChangesOutToErr() {
		doWithSystemStreams(() -> {
			ByteArrayOutputStream errBytes = new ByteArrayOutputStream();
			PrintStream err = new PrintStream(errBytes);
			System.setErr(err);
			SystemStreams.reconfigureSystem();
			System.out.print("foo");
			System.err.print("bar");
			assertThat(errBytes.toString()).isEqualTo("foobar");
		});
	}

	@Test
	public void reconfigureSystemProvidesAccessToOriginalSystemOut() throws Exception {
		doWithSystemStreams(() -> {
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outBytes);
			System.setOut(out);
			SystemStreams streams = SystemStreams.reconfigureSystem();
			System.out.print("foo");
			streams.out().print("bar");
			assertThat(outBytes.toString()).isEqualTo("bar");
		});
	}

	@Test
	public void reconfigureSystemProvidesAccessToOriginalSystemIn() throws Exception {
		doWithSystemStreams(() -> {
			ByteArrayInputStream inBytes = new ByteArrayInputStream("foo".getBytes());
			System.setIn(inBytes);
			SystemStreams streams = SystemStreams.reconfigureSystem();
			assertThat(streams.in()).isSameAs(inBytes);
		});
	}

	private void doWithSystemStreams(Runnable runnable) {
		InputStream in = System.in;
		PrintStream out = System.out;
		PrintStream err = System.err;
		try {
			runnable.run();
		}
		finally {
			System.setIn(in);
			System.setOut(out);
			System.setErr(err);
		}
	}

}
