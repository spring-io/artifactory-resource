/*
 * Copyright 2017-2018 the original author or authors.
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

package io.spring.concourse.artifactoryresource.maven;

import java.util.regex.Pattern;

import org.springframework.util.Assert;

/**
 * Different styles of maven version numbers.
 *
 * @author Phillip Webb
 */
public enum MavenVersionType {

	/**
	 * A timestamp based snapshot, for example {@code 1.0.0.BUILD-20171005.194031-1}.
	 */
	TIMESTAMP_SNAPSHOT,

	/**
	 * A regular snapshot, for example {@code 1.0.0.BUILD-SNAPSHOT}.
	 */
	SNAPSHOT,

	/**
	 * A fixed version, for example {@code 1.0.0.RELEASE}.
	 */
	FIXED;

	private static final String SNAPSHOT_VERSION = "SNAPSHOT";

	private static final Pattern VERSION_FILE_PATTERN = Pattern
			.compile("^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$");

	public static MavenVersionType fromVersion(String version) {
		Assert.hasLength(version, "Version must not be empty");
		if (version.regionMatches(true, version.length() - SNAPSHOT_VERSION.length(),
				SNAPSHOT_VERSION, 0, SNAPSHOT_VERSION.length())) {
			return SNAPSHOT;
		}
		if (VERSION_FILE_PATTERN.matcher(version).matches()) {
			return TIMESTAMP_SNAPSHOT;
		}
		return FIXED;
	}

}
