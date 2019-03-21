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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Tests for {@link SystemOutput}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class SystemOutputTests {

	@Test
	public void writeSerializesJson() throws Exception {
		MockSystemStreams systemStreams = new MockSystemStreams("");
		SystemOutput output = new SystemOutput(systemStreams, new ObjectMapper());
		output.write(new String[] { "foo" });
		String actual = new String(systemStreams.getOutBytes());
		JSONAssert.assertEquals("[\"foo\"]", actual, false);
	}

}
