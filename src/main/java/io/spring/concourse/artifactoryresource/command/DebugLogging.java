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

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

/**
 * Utility to enable debug logging.
 *
 * @author Phillip Webb
 */
final class DebugLogging {

	private DebugLogging() {
	}

	public static void setEnabled(boolean enabled) {
		if (enabled) {
			LoggingSystem system = LoggingSystem
					.get(Thread.currentThread().getContextClassLoader());
			system.setLogLevel("org.springframework", LogLevel.DEBUG);
			system.setLogLevel("io.spring.concourse.artifactoryresource", LogLevel.DEBUG);
		}
	}

}
