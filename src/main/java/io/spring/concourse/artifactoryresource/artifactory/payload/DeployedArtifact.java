/*
 * Copyright 2017-2023 the original author or authors.
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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.concourse.artifactoryresource.jackson.JsonIsoDateFormat;

import org.springframework.util.Assert;

/**
 * Details of an already deployed artifact.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class DeployedArtifact {

	private final String repo;

	private final String path;

	private final String type;

	private final String name;

	private final long size;

	@JsonIsoDateFormat
	private final Instant created;

	@JsonProperty("created-by")
	private final String createdBy;

	@JsonIsoDateFormat
	private final Instant modified;

	@JsonProperty("modified-by")
	private final String modifiedBy;

	@JsonIsoDateFormat
	private final Instant updated;

	public DeployedArtifact(String repo, String name, String path) {
		this(repo, name, path, null, 0, null, null, null, null, null);
	}

	@JsonCreator
	public DeployedArtifact(@JsonProperty("repo") String repo, @JsonProperty("name") String name,
			@JsonProperty("path") String path, @JsonProperty("type") String type, @JsonProperty("size") long size,
			@JsonProperty("created") Instant created, @JsonProperty("created-by") String createdBy,
			@JsonProperty("modified") Instant modified, @JsonProperty("modified-by") String modifiedBy,
			@JsonProperty("updated") Instant updated) {
		Assert.hasText(repo, "Repo must not be empty");
		Assert.hasText(name, "Name must not be empty");
		Assert.hasText(path, "Path must not be empty");
		this.repo = repo;
		this.path = path;
		this.type = type;
		this.name = name;
		this.size = size;
		this.created = created;
		this.createdBy = createdBy;
		this.modified = modified;
		this.modifiedBy = modifiedBy;
		this.updated = updated;
	}

	public String getRepo() {
		return this.repo;
	}

	public String getPath() {
		return this.path;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public Instant getCreated() {
		return this.created;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Instant getModified() {
		return this.modified;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public Instant getUpdated() {
		return this.updated;
	}

	public long getSize() {
		return this.size;
	}

}
