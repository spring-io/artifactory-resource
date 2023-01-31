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

package io.spring.concourse.artifactoryresource.artifactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A build number.
 *
 * @author Phillip Webb
 */
public final class BuildNumber {

	private final String value;

	public BuildNumber(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return this.value.equals(((BuildNumber) obj).value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value;
	}

	public static BuildNumber of(String number) {
		return of(null, number);
	}

	public static BuildNumber of(String prefix, String number) {
		Assert.hasText(number, "Build number must not be empty");
		return new BuildNumber(StringUtils.hasText(prefix) ? prefix + number : number);
	}

}
