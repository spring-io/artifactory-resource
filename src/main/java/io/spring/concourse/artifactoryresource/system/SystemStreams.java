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

package io.spring.concourse.artifactoryresource.system;

import java.io.InputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to {@code stdin} and {@code stdout} and re-maps {@code System.out} to
 * {@code System.err} to prevent accidental log output.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class SystemStreams {

	private static final Logger logger = LoggerFactory.getLogger(SystemStreams.class);

	private static SystemStreams instance;

	private final InputStream in;

	private final PrintStream out;

	protected SystemStreams(InputStream in, PrintStream out) {
		this.in = in;
		this.out = out;
	}

	/**
	 * Return {@code stdin}.
	 * @return the {@code stdin} input stream
	 */
	public InputStream in() {
		return this.in;
	}

	/**
	 * Return {@code stdout}.
	 * @return the {@code stdout} print stream
	 */
	public PrintStream out() {
		return this.out;
	}

	/**
	 * Return a {@link SystemStreams} instance following reconfiguration of the standard
	 * {@link System} {@code in} and {@code err} streams. The {@code System.out} stream is
	 * redirected to {@code System.err}.
	 * @return the system streams instance
	 */
	public static SystemStreams reconfigureSystem() {
		logger.trace("Reconfiguring system system streams");
		PrintStream out = System.out;
		System.setOut(System.err);
		instance = new SystemStreams(System.in, out);
		return instance;
	}

	public static SystemStreams instance() {
		if (instance == null) {
			instance = new SystemStreams(System.in, System.out);
		}
		return instance;
	}

}
