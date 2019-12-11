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

package io.spring.concourse.artifactoryresource.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

/**
 * Support for checksums used by the artifactory resource.
 *
 * @author Phillip Webb
 */
public enum Checksum {

	/**
	 * MD5 Checksum.
	 */
	MD5("MD5", 32),

	/**
	 * SHA-1 Checksum.
	 */
	SHA1("SHA-1", 40);

	private final String algorithm;

	private final int length;

	Checksum(String algorithm, int length) {
		this.algorithm = algorithm;
		this.length = length;
	}

	/**
	 * Return the file extension used for the checksum.
	 * @return the file extension.
	 */
	public String getFileExtension() {
		return "." + name().toLowerCase();
	}

	/**
	 * Validate the given checksum value.
	 * @param checksum the checksum value to test
	 * @throws IllegalArgumentException on an invalid checksum
	 */
	public void validate(String checksum) {
		Assert.hasText(checksum, name() + " must not be empty");
		Assert.isTrue(checksum.length() == this.length, name() + " must be " + this.length + " characters long");
	}

	private DigestInputStream getDigestStream(InputStream content) throws NoSuchAlgorithmException {
		return new DigestInputStream(content, MessageDigest.getInstance(this.algorithm));
	}

	/**
	 * Generate checksum files for the given source file.
	 * @param source the source file
	 * @throws IOException on file read error
	 */
	public static void generateChecksumFiles(File source) throws IOException {
		if (!isChecksumFile(source.getName())) {
			Map<Checksum, String> checksums = calculateAll(new FileSystemResource(source));
			for (Map.Entry<Checksum, String> entry : checksums.entrySet()) {
				File file = new File(source.getParentFile(), source.getName() + entry.getKey().getFileExtension());
				FileCopyUtils.copy(entry.getValue(), new FileWriter(file));
			}
		}
	}

	/**
	 * Returns true if the specified path is for a checksum file.
	 * @param path the path to check
	 * @return if the path is a checksum file
	 */
	public static boolean isChecksumFile(String path) {
		return getFileExtensions().anyMatch(path.toLowerCase()::endsWith);
	}

	/**
	 * Return all checksum file extensions.
	 * @return the checksum file extensions
	 */
	public static Stream<String> getFileExtensions() {
		return Arrays.stream(values()).map(Checksum::getFileExtension);
	}

	/**
	 * Calculate all checksums for the specified content.
	 * @param content the source content
	 * @return a map of all checksums
	 */
	public static Map<Checksum, String> calculateAll(Resource content) {
		try {
			Assert.notNull(content, "Content must not be null");
			return calculateAll(content.getInputStream());
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	/**
	 * Calculate all checksums for the specified content.
	 * @param content the source content
	 * @return a map of all checksums
	 */
	public static Map<Checksum, String> calculateAll(String content) {
		Assert.notNull(content, "Content must not be null");
		return calculateAll(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}

	private static Map<Checksum, String> calculateAll(InputStream content) {
		Assert.notNull(content, "Content must not be null");
		try {
			try {
				Map<Checksum, DigestInputStream> streams = new LinkedHashMap<>();
				for (Checksum checksum : values()) {
					DigestInputStream digestStream = checksum.getDigestStream(content);
					streams.put(checksum, digestStream);
					content = digestStream;
				}
				StreamUtils.drain(content);
				return getDigests(streams);
			}
			finally {
				content.close();
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private static Map<Checksum, String> getDigests(Map<Checksum, DigestInputStream> streams) {
		Map<Checksum, String> checksums = new LinkedHashMap<>(streams.size());
		streams.forEach((checksum, stream) -> checksums.put(checksum, getDigestHex(stream)));
		return checksums;
	}

	private static String getDigestHex(DigestInputStream inputStream) {
		byte[] digest = inputStream.getMessageDigest().digest();
		return DatatypeConverter.printHexBinary(digest).toLowerCase();
	}

}
