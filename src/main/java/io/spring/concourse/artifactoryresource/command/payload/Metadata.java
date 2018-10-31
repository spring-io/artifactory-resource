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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A metadata item that can be returned as part of {@link InRequest} or
 * {@link OutResponse}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class Metadata {

	private final String name;

	private final Object value;

	public Metadata(String name, Object value) {
		Assert.hasText(name, "Name must not be empty");
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("name", this.name)
				.append("value", this.value).toString();
	}

}
