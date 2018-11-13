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

import java.util.Map;

import io.spring.concourse.artifactoryresource.io.Checksum;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;

/**
 * SHA1 and MD5 Checksums supported by artifactory.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class Checksums {

	private final String sha1;

	private final String md5;

	public Checksums(String sha1, String md5) {
		Checksum.SHA1.validate(sha1);
		Checksum.MD5.validate(md5);
		this.sha1 = sha1;
		this.md5 = md5;
	}

	public String getSha1() {
		return this.sha1;
	}

	public String getMd5() {
		return this.md5;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("sha1", this.sha1).append("md5", this.md5)
				.toString();
	}

	public static Checksums calculate(Resource content) {
		Map<Checksum, String> all = Checksum.calculateAll(content);
		return new Checksums(all.get(Checksum.SHA1), all.get(Checksum.MD5));
	}

}
