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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;

/**
 * A single build artifact included in {@link BuildInfo}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @see BuildInfo
 */
public class BuildArtifact {

	private final String type;

	private final String sha1;

	private final String md5;

	private final String name;

	@JsonCreator
	public BuildArtifact(@JsonProperty("type") String type,
			@JsonProperty("sha1") String sha1, @JsonProperty("md5") String md5,
			@JsonProperty("name") String name) {
		Assert.hasText(type, "Type must not be empty");
		Assert.hasText(sha1, "SHA1 must not be empty");
		Assert.hasText(md5, "MD5 must not be empty");
		Assert.hasText(name, "Name must not be empty");
		this.type = type;
		this.sha1 = sha1;
		this.md5 = md5;
		this.name = name;
	}

	public String getType() {
		return this.type;
	}

	public String getSha1() {
		return this.sha1;
	}

	public String getMd5() {
		return this.md5;
	}

	public String getName() {
		return this.name;
	}

}
