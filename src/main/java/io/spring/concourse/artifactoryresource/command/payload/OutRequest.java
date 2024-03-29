/*
 * Copyright 2017-2024 the original author or authors.
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

package io.spring.concourse.artifactoryresource.command.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Request to the {@code "/opt/resource/out"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class OutRequest {

	private final Source source;

	private final Params params;

	@JsonCreator
	public OutRequest(@JsonProperty("source") Source source, @JsonProperty("params") Params params) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(params, "Params must not be null");
		this.source = source;
		this.params = params;
	}

	public Source getSource() {
		return this.source;
	}

	public Params getParams() {
		return this.params;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("source", this.source).append("params", this.params).toString();
	}

	/**
	 * Parameters for the {@link OutRequest}.
	 */
	public static class Params {

		private final boolean debug;

		private final String repo;

		private final String buildNumber;

		private final String folder;

		private final List<String> include;

		private final List<String> exclude;

		private final String moduleLayout;

		private final String buildUri;

		private final String buildProperties;

		private final Boolean stripSnapshotTimestamps;

		private final boolean disableChecksumUploads;

		private final List<ArtifactSet> artifactSet;

		private final int threads;

		private final String signingKey;

		private final String signingPassphrase;

		@JsonCreator
		public Params(@JsonProperty("debug") Boolean debug, @JsonProperty("repo") String repo,
				@JsonProperty("build_number") String buildNumber, @JsonProperty("folder") String folder,
				@JsonProperty("include") List<String> include, @JsonProperty("exclude") List<String> exclude,
				@JsonProperty("module_layout") String moduleLayout, @JsonProperty("build_uri") String buildUri,
				@JsonProperty("build_properties") String buildProperties,
				@JsonProperty("strip_snapshot_timestamps") Boolean stripSnapshotTimestamps,
				@JsonProperty("disable_checksum_uploads") Boolean disableChecksumUploads,
				@JsonProperty("artifact_set") List<ArtifactSet> artifactSet, @JsonProperty("threads") Integer threads,
				@JsonProperty("signing_key") String signingKey,
				@JsonProperty("signing_passphrase") String signingPassphrase) {
			Assert.hasText(repo, "Repo must not be empty");
			Assert.hasText(folder, "Folder must not be empty");
			this.debug = (debug != null) ? debug : false;
			this.buildNumber = buildNumber;
			this.repo = repo;
			this.folder = folder;
			this.include = (include != null) ? Collections.unmodifiableList(new ArrayList<>(include))
					: Collections.emptyList();
			this.exclude = (exclude != null) ? Collections.unmodifiableList(new ArrayList<>(exclude))
					: Collections.emptyList();
			this.moduleLayout = moduleLayout;
			this.buildUri = buildUri;
			this.buildProperties = buildProperties;
			this.stripSnapshotTimestamps = stripSnapshotTimestamps;
			this.disableChecksumUploads = (disableChecksumUploads != null) ? disableChecksumUploads : false;
			this.artifactSet = (artifactSet != null) ? Collections.unmodifiableList(new ArrayList<>(artifactSet))
					: Collections.emptyList();
			this.threads = Integer.max(1, (threads != null) ? threads : 1);
			this.signingKey = signingKey;
			this.signingPassphrase = signingPassphrase;
		}

		public boolean isDebug() {
			return this.debug;
		}

		public String getRepo() {
			return this.repo;
		}

		public String getBuildNumber() {
			return this.buildNumber;
		}

		public String getFolder() {
			return this.folder;
		}

		public List<String> getInclude() {
			return this.include;
		}

		public List<String> getExclude() {
			return this.exclude;
		}

		public String getModuleLayout() {
			return this.moduleLayout;
		}

		public String getBuildUri() {
			return this.buildUri;
		}

		public String getBuildProperties() {
			return this.buildProperties;
		}

		public boolean isStripSnapshotTimestamps() {
			if (this.stripSnapshotTimestamps != null) {
				return this.stripSnapshotTimestamps;
			}
			return !StringUtils.hasText(this.moduleLayout) || "maven".equalsIgnoreCase(this.moduleLayout);
		}

		public boolean isDisableChecksumUploads() {
			return this.disableChecksumUploads;
		}

		public List<ArtifactSet> getArtifactSet() {
			return this.artifactSet;
		}

		public int getThreads() {
			return this.threads;
		}

		public String getSigningKey() {
			return this.signingKey;
		}

		public String getSigningPassphrase() {
			return this.signingPassphrase;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("buildNumber", this.buildNumber)
				.append("folder", this.folder)
				.append("include", this.include)
				.append("exclude", this.exclude)
				.append("moduleLayout", this.moduleLayout)
				.append("buildUri", this.buildUri)
				.append("stripSnapshotTimestamps", this.stripSnapshotTimestamps)
				.append("artifactSet", this.artifactSet)
				.append("threads", this.threads)
				.append("signingKey", (StringUtils.hasText(this.signingKey)) ? "<set>" : "<not set>")
				.append("signingPassphrase", (StringUtils.hasText(this.signingPassphrase)) ? "<set>" : "<not set>")
				.toString();
		}

	}

	/**
	 * An artifact set for additional configuration.
	 */
	public static class ArtifactSet {

		private final List<String> include;

		private final List<String> exclude;

		private final Map<String, String> properties;

		@JsonCreator
		public ArtifactSet(@JsonProperty("include") List<String> include, @JsonProperty("exclude") List<String> exclude,
				@JsonProperty("properties") Map<String, String> properties) {
			this.include = (include != null) ? Collections.unmodifiableList(new ArrayList<>(include))
					: Collections.emptyList();
			this.exclude = (exclude != null) ? Collections.unmodifiableList(new ArrayList<>(exclude))
					: Collections.emptyList();
			this.properties = (properties != null) ? Collections.unmodifiableMap(new LinkedHashMap<>(properties))
					: Collections.emptyMap();
		}

		public List<String> getInclude() {
			return this.include;
		}

		public List<String> getExclude() {
			return this.exclude;
		}

		public Map<String, String> getProperties() {
			return this.properties;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("include", this.include)
				.append("exclude", this.exclude)
				.append("properties", this.properties)
				.toString();
		}

	}

}
