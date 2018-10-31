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

package io.spring.concourse.artifactoryresource.command.payload;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Response from the {@code "/opt/resource/check"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class CheckResponse {

	private final List<Version> versions;

	@JsonCreator
	public CheckResponse(List<Version> versions) {
		Assert.notNull(versions, "Versions must not be null");
		this.versions = Collections.unmodifiableList(versions);
	}

	@JsonValue
	public List<Version> getVersions() {
		return this.versions;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("versions", this.versions).toString();
	}

}
