/*
 * Copyright 2017 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Request to the {@code "/opt/resource/in"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class InRequest {

	private final Source source;

	private final Version version;

	private final Params params;

	@JsonCreator
	public InRequest(@JsonProperty("source") Source source,
			@JsonProperty("version") Version version,
			@JsonProperty("params") Params params) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(version, "Version must not be null");
		this.source = source;
		this.version = version;
		this.params = (params == null ? new Params() : params);
	}

	public Source getSource() {
		return this.source;
	}

	public Version getVersion() {
		return this.version;
	}

	public Params getParams() {
		return this.params;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("source", this.source)
				.append("version", this.version).append("params", this.params).toString();
	}

	/**
	 * Parameters for the {@link InRequest}.
	 */
	public static class Params {

		private final boolean debug;

		private final boolean generateMavenMetadata;

		public Params() {
			this(null, null);
		}

		@JsonCreator
		public Params(@JsonProperty("debug") Boolean debug,
				@JsonProperty("generate_maven_metadata") Boolean generateMavenMetadata) {
			this.debug = (debug == null ? false : debug);
			this.generateMavenMetadata = (generateMavenMetadata == null ? true
					: generateMavenMetadata);
		}

		public boolean isDebug() {
			return this.debug;
		}

		public boolean isGenerateMavenMetadata() {
			return this.generateMavenMetadata;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this)
					.append("generateMavenMetadata", this.generateMavenMetadata)
					.toString();
		}

	}

}
