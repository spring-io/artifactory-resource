/*
 * Copyright 2017-2021 the original author or authors.
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

package io.spring.concourse.artifactoryresource.openpgp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ArmoredAsciiSigner}.
 *
 * @author Phillip Webb
 */
class ArmoredAsciiSignerTests {

	private static final Clock FIXED = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"));

	private File signingKeyFile;

	private Resource signingKeyResource;

	private String signingKeyContent;

	private String passphrase = "password";

	private File sourceFile;

	private String sourceContent;

	private String expectedSignature;

	private File temp;

	@BeforeEach
	void setup(@TempDir File temp) throws Exception {
		this.temp = temp;
		this.signingKeyFile = copyClasspathFile("test-private.txt");
		this.signingKeyResource = new FileSystemResource(this.signingKeyFile);
		this.signingKeyContent = copyToString(this.signingKeyFile);
		this.sourceFile = copyClasspathFile("source.txt");
		this.sourceContent = copyToString(this.sourceFile);
		this.expectedSignature = copyToString(ArmoredAsciiSignerTests.class.getResourceAsStream("expected.asc"));
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsKeyReturnsSigner() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThat(signer.sign(this.sourceContent)).isEqualTo(this.expectedSignature);
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsFileReturnsSigner() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyFile.getAbsolutePath(),
				this.passphrase);
		assertThat(signer.sign(this.sourceContent)).isEqualTo(this.expectedSignature);
	}

	@Test
	void getWithStringSigningKeyWhenClockIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(null, this.signingKeyContent, this.passphrase))
				.withMessage("Clock must not be null");
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, (String) null, this.passphrase))
				.withMessage("SigningKey must not be null");
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsEmptyThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, "", this.passphrase))
				.withMessage("SigningKey must not be empty");
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsMultiLineWithoutHeaderThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, "ab\ncd", this.passphrase))
				.withMessage("Signing key is not does not contain a PGP private key block "
						+ "and does not reference a file");
	}

	@Test
	void getWithStringSigningKeyWhenSigningKeyIsMalformedThrowsException() throws Exception {
		String signingKey = copyToString(getClass().getResourceAsStream("test-bad-private.txt"));
		assertThatIllegalStateException().isThrownBy(() -> ArmoredAsciiSigner.get(signingKey, this.passphrase))
				.withMessage("Unable to read signing key");
	}

	@Test
	void getWithStringSigningKeyWhenPassphraseIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, null))
				.withMessage("Passphrase must not be null");
	}

	@Test
	void getWithStringSigningKeyWhenPassphraseIsWrongThrowsException() throws IOException {
		assertThatIllegalStateException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyFile, "bad"))
				.withMessage("Unable to extract private key");
	}

	@Test
	void getWithFileSigningKeyKeyReturnsSigner() throws IOException {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyFile, this.passphrase);
		assertThat(signer.sign(this.sourceContent)).isEqualTo(this.expectedSignature);
	}

	@Test
	void getWithFileSigningKeyWhenClockIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(null, this.signingKeyFile, this.passphrase))
				.withMessage("Clock must not be null");
	}

	@Test
	void getWithFileSigningKeyWhenSigningKeyIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, (File) null, this.passphrase))
				.withMessage("SigningKey must not be null");
	}

	@Test
	void getWithFileSigningKeyWhenSigningKeyIsMalformedThrowsException() throws Exception {
		File signingKey = copyClasspathFile("test-bad-private.txt");
		assertThatIllegalStateException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, signingKey, this.passphrase))
				.withMessage("Unable to read signing key");
	}

	@Test
	void getWithFileSigningKeyWhenPassphraseIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyFile, null))
				.withMessage("Passphrase must not be null");
	}

	@Test
	void getWithFileSigningKeyWhenPassphraseIsWrongThrowsException() {
		assertThatIllegalStateException().isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyFile, "bad"))
				.withMessage("Unable to extract private key");
	}

	@Test
	void getWithInputStreamSourceSigningKeyKeyReturnsSigner() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyResource, this.passphrase);
		assertThat(signer.sign(this.sourceContent)).isEqualTo(this.expectedSignature);
	}

	@Test
	void getWithInputStreamSourceSigningKeyWhenClockIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(null, this.signingKeyResource, this.passphrase))
				.withMessage("Clock must not be null");
	}

	@Test
	void getWithInputStreamSourceSigningKeyWhenSigningKeyIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, (InputStreamSource) null, this.passphrase))
				.withMessage("SigningKey must not be null");
	}

	@Test
	void getWithInputStreamSourceSigningKeyWhenSigningKeyIsMalformedThrowsException() throws Exception {
		File signingKeyFile = copyClasspathFile("test-bad-private.txt");
		Resource signingKeyResource = new FileSystemResource(signingKeyFile);
		assertThatIllegalStateException().isThrownBy(() -> ArmoredAsciiSigner.get(signingKeyResource, this.passphrase))
				.withMessage("Unable to read signing key");
	}

	@Test
	void getWithInputStreamSourceSigningKeyWhenPassphraseIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyResource, null))
				.withMessage("Passphrase must not be null");
	}

	@Test
	void getWithInputStreamSourceSigningKeyWhenPassphraseIsWrongThrowsException() {
		assertThatIllegalStateException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, this.signingKeyResource, "bad"))
				.withMessage("Unable to extract private key");
	}

	@Test
	void getWithInputStreamSigningKeyKeyReturnsSigner() {
		assertThatIllegalStateException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, new FileInputStream(this.signingKeyFile), "bad"))
				.withMessage("Unable to extract private key");
	}

	@Test
	void getWithInputStreamSigningKeyWhenClockIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(
						() -> ArmoredAsciiSigner.get(null, new FileInputStream(this.signingKeyFile), this.passphrase))
				.withMessage("Clock must not be null");
	}

	@Test
	void getWithInputStreamSigningKeyWhenSigningKeyIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, (InputStream) null, this.passphrase))
				.withMessage("SigningKey must not be null");
	}

	@Test
	void getWithInputStreamSigningKeyWhenSigningKeyIsMalformedThrowsException() throws Exception {
		File signingKey = copyClasspathFile("test-bad-private.txt");
		assertThatIllegalStateException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, new FileInputStream(signingKey), this.passphrase))
				.withMessage("Unable to read signing key");
	}

	@Test
	void getWithInputStreamSigningKeyWhenPassphraseIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, new FileInputStream(this.signingKeyFile), null))
				.withMessage("Passphrase must not be null");
	}

	@Test
	void getWithInputStreamSigningKeyWhenPassphraseIsWrongThrowsException() {
		assertThatIllegalStateException()
				.isThrownBy(() -> ArmoredAsciiSigner.get(FIXED, new FileInputStream(this.signingKeyFile), "bad"))
				.withMessage("Unable to extract private key");
	}

	@Test
	void signWithStringReturnsSignature() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThat(signer.sign(this.sourceContent)).isEqualTo(this.expectedSignature);
	}

	@Test
	void signWithStringWhenSourceIsNullThrowsException() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThatIllegalArgumentException().isThrownBy(() -> signer.sign((String) null))
				.withMessage("Source must not be null");
	}

	@Test
	void signWithInputStreamSourceReturnsSignature() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThat(signer.sign(new FileSystemResource(this.sourceFile))).isEqualTo(this.expectedSignature);
	}

	@Test
	void signWithInputStreamSourceWhenSourceIsNullThrowsException() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThatIllegalArgumentException().isThrownBy(() -> signer.sign((InputStreamSource) null))
				.withMessage("Source must not be null");
	}

	@Test
	void signWithInputStreamReturnsSignature() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThat(signer.sign(new FileInputStream(this.sourceFile))).isEqualTo(this.expectedSignature);
	}

	@Test
	void signWithInputStreamWhenSourceIsNullThrowsException() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThatIllegalArgumentException().isThrownBy(() -> signer.sign((InputStream) null))
				.withMessage("Source must not be null");
	}

	@Test
	void signWithInputStreamAndOutputStreamWritesSignature() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		ByteArrayOutputStream destination = new ByteArrayOutputStream();
		signer.sign(new FileInputStream(this.sourceFile), destination);
		assertThat(destination.toByteArray()).asString(StandardCharsets.UTF_8).isEqualTo(this.expectedSignature);
	}

	@Test
	void signWithInputStreamAndOutputStreamWritesWhenSourceIsNullThrowsException() throws IOException {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThatIllegalArgumentException().isThrownBy(() -> signer.sign(null, new ByteArrayOutputStream()))
				.withMessage("Source must not be null");
	}

	@Test
	void signWithInputStreamAndOutputStreamWritesWhenDestinationIsNullThrowsException() throws Exception {
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(FIXED, this.signingKeyContent, this.passphrase);
		assertThatIllegalArgumentException().isThrownBy(() -> signer.sign(new FileInputStream(this.sourceFile), null))
				.withMessage("Destination must not be null");
	}

	@Test
	void signWithClockTickReturnsDifferentContent() throws Exception {
		Clock clock = mock(Clock.class);
		given(clock.instant()).willReturn(FIXED.instant(), Clock.offset(FIXED, Duration.ofSeconds(2)).instant());
		ArmoredAsciiSigner signer = ArmoredAsciiSigner.get(clock, this.signingKeyContent, this.passphrase);
		String signatureOne = signer.sign(this.sourceContent);
		String signatureTwo = signer.sign(this.sourceContent);
		assertThat(signatureOne).isNotEqualTo(signatureTwo);
	}

	private File copyClasspathFile(String name) throws IOException {
		File file = new File(this.temp, name);
		FileCopyUtils.copy(ArmoredAsciiSignerTests.class.getResourceAsStream(name), new FileOutputStream(file));
		return file;
	}

	private String copyToString(File file) throws IOException {
		return copyToString(new FileInputStream(file));
	}

	private String copyToString(InputStream inputStream) throws IOException {
		return new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
	}

}
