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

package io.spring.concourse.artifactoryresource.artifactory.payload;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.springframework.core.io.Resource;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

/**
 * SHA1 and MD5 Checksums supported by artifactory.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public final class Checksums {

	private static final int SHA1_LENGTH = 40;

	private static final int MD5_LENGTH = 32;

	private final String sha1;

	private final String md5;

	public Checksums(String sha1, String md5) {
		Assert.hasText(sha1, "SHA1 must not be empty");
		Assert.isTrue(sha1.length() == SHA1_LENGTH,
				"SHA1 must be " + SHA1_LENGTH + " characters long");
		Assert.hasText(md5, "MD5 must not be empty");
		Assert.isTrue(md5.length() == MD5_LENGTH,
				"MD5 must be " + MD5_LENGTH + " characters long");
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
		try {
			Assert.notNull(content, "Content must not be null");
			return calculate(content.getInputStream());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static Checksums calculate(InputStream content) throws IOException {
		Assert.notNull(content, "Content must not be null");
		try {
			DigestInputStream sha1 = new DigestInputStream(content,
					MessageDigest.getInstance("SHA-1"));
			DigestInputStream md5 = new DigestInputStream(sha1,
					MessageDigest.getInstance("MD5"));
			StreamUtils.drain(md5);
			return new Checksums(getDigestHex(sha1), getDigestHex(md5));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		finally {
			content.close();
		}
	}

	private static String getDigestHex(DigestInputStream sha1) {
		return DatatypeConverter.printHexBinary(sha1.getMessageDigest().digest())
				.toLowerCase();
	}

}
