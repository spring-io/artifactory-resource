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

package io.spring.concourse.artifactoryresource.command.payload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Response from the {@code "/opt/resource/out"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class OutResponse {

	private final Version version;

	private final List<Metadata> metadata;

	public OutResponse(Version version) {
		this(version, null);
	}

	public OutResponse(Version version, List<Metadata> metadata) {
		Assert.notNull(version, "Version must not be null");
		this.version = version;
		this.metadata = (metadata != null)
				? Collections.unmodifiableList(new ArrayList<>(metadata))
				: Collections.emptyList();
	}

	public Version getVersion() {
		return this.version;
	}

	public List<Metadata> getMetadata() {
		return this.metadata;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("version", this.version)
				.append("metadata", this.metadata).toString();
	}

}
