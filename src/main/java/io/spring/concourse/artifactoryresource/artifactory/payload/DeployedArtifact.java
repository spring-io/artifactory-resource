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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.util.Assert;

/**
 * Details of an already deployed artifact.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class DeployedArtifact {

	private String repo;

	private String path;

	private String type;

	private String name;

	private long size;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date created;

	@JsonProperty("created-by")
	private String createdBy;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date modified;

	@JsonProperty("modified-by")
	private String modifiedBy;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private Date updated;

	public DeployedArtifact(String repo, String name, String path) {
		this(repo, name, path, null, 0, null, null, null, null, null);
	}

	@JsonCreator
	public DeployedArtifact(@JsonProperty("repo") String repo,
			@JsonProperty("name") String name, @JsonProperty("path") String path,
			@JsonProperty("type") String type, @JsonProperty("size") long size,
			@JsonProperty("created") Date created,
			@JsonProperty("created-by") String createdBy,
			@JsonProperty("modified") Date modified,
			@JsonProperty("modified-by") String modifiedBy,
			@JsonProperty("updated") Date updated) {
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

	public Date getCreated() {
		return this.created;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public Date getModified() {
		return this.modified;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public Date getUpdated() {
		return this.updated;
	}

	public long getSize() {
		return this.size;
	}

}
