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

package io.spring.concourse.artifactoryresource.openpgp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.time.Clock;
import java.util.Date;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import org.springframework.core.io.InputStreamSource;
import org.springframework.util.Assert;

/**
 * Utility to sign artifacts by generating armored ASCII.
 *
 * @author Phillip Webb
 */
public final class ArmoredAsciiSigner {

	private static final String PRIVATE_KEY_BLOCK_HEADER = "-----BEGIN PGP PRIVATE KEY BLOCK-----";

	private static final JcaKeyFingerprintCalculator FINGERPRINT_CALCULATOR = new JcaKeyFingerprintCalculator();

	private static final int BUFFER_SIZE = 4096;

	static {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private final PGPSecretKey signingKey;

	private final PGPPrivateKey privateKey;

	private final JcaPGPContentSignerBuilder contentSigner;

	private final Clock clock;

	private ArmoredAsciiSigner(Clock clock, InputStream signingKeyInputStream, String passphrase) throws IOException {
		PGPSecretKey signingKey = getSigningKey(signingKeyInputStream);
		this.clock = clock;
		this.signingKey = signingKey;
		this.privateKey = extractPrivateKey(passphrase, signingKey);
		this.contentSigner = getContentSigner(signingKey.getPublicKey().getAlgorithm());
	}

	private PGPSecretKey getSigningKey(InputStream inputStream) throws IOException {
		try {
			try (InputStream decoderStream = PGPUtil.getDecoderStream(inputStream)) {
				PGPSecretKeyRingCollection keyrings = new PGPSecretKeyRingCollection(decoderStream,
						FINGERPRINT_CALCULATOR);
				return getSigningKey(keyrings);
			}
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to read signing key", ex);
		}
	}

	private PGPSecretKey getSigningKey(PGPSecretKeyRingCollection keyrings) {
		for (PGPSecretKeyRing keyring : keyrings) {
			Iterable<PGPSecretKey> secretKeys = keyring::getSecretKeys;
			for (PGPSecretKey candidate : secretKeys) {
				if (candidate.isSigningKey()) {
					return candidate;
				}
			}
		}
		throw new IllegalArgumentException("Keyring does not contain a suitable signing key");
	}

	private PGPPrivateKey extractPrivateKey(String passphrase, PGPSecretKey signingKey) {
		try {
			return signingKey.extractPrivateKey(getDecryptorFactory(passphrase));
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to extract private key", ex);
		}
	}

	private PBESecretKeyDecryptor getDecryptorFactory(String passphrase) throws PGPException {
		return new JcePBESecretKeyDecryptorBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.build(passphrase.toCharArray());
	}

	private JcaPGPContentSignerBuilder getContentSigner(int signingAlgorithm) {
		return new JcaPGPContentSignerBuilder(signingAlgorithm, HashAlgorithmTags.SHA256)
				.setProvider(BouncyCastleProvider.PROVIDER_NAME);
	}

	/**
	 * Sign the given source.
	 * @param source the source to sign
	 * @return the signature
	 * @throws IOException on IO error
	 */
	public String sign(String source) throws IOException {
		Assert.notNull(source, "Source must not be null");
		return sign(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * Sign the given source.
	 * @param source the source to sign
	 * @return the signature
	 * @throws IOException on IO error
	 */
	public String sign(InputStreamSource source) throws IOException {
		Assert.notNull(source, "Source must not be null");
		return sign(source.getInputStream());
	}

	/**
	 * Sign the given source.
	 * @param source the source to sign (will be closed after use)
	 * @return the signature
	 * @throws IOException on IO error
	 */
	public String sign(InputStream source) throws IOException {
		Assert.notNull(source, "Source must not be null");
		ByteArrayOutputStream destination = new ByteArrayOutputStream();
		sign(source, destination);
		return new String(destination.toByteArray(), StandardCharsets.UTF_8);
	}

	/**
	 * Sign the given source.
	 * @param source the source to sign (will be closed after use)
	 * @param destination the signature destination
	 * @throws IOException on IO error
	 */
	public void sign(InputStream source, OutputStream destination) throws IOException {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(destination, "Destination must not be null");
		try (ArmoredOutputStream armoredOutputStream = new ArmoredOutputStream(destination)) {
			armoredOutputStream.setHeader(ArmoredOutputStream.VERSION_HDR, null);
			sign(source, armoredOutputStream);
		}
		catch (PGPException ex) {
			throw new IllegalStateException(ex);
		}
		finally {
			destination.close();
		}
	}

	private void sign(InputStream source, ArmoredOutputStream destination) throws PGPException, IOException {
		PGPSignatureGenerator signatureGenerator = getSignatureGenerator();
		updateSignatureGenerator(source, signatureGenerator);
		signatureGenerator.generate().encode(destination);
	}

	private PGPSignatureGenerator getSignatureGenerator() throws PGPException {
		PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(this.contentSigner);
		signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, this.privateKey);
		PGPSignatureSubpacketGenerator subpacketGenerator = getSignatureSubpacketGenerator();
		signatureGenerator.setHashedSubpackets(subpacketGenerator.generate());
		return signatureGenerator;
	}

	private PGPSignatureSubpacketGenerator getSignatureSubpacketGenerator() {
		PGPSignatureSubpacketGenerator subpacketGenerator = new PGPSignatureSubpacketGenerator();
		subpacketGenerator.setIssuerFingerprint(false, this.signingKey.getPublicKey());
		subpacketGenerator.setSignatureCreationTime(false, Date.from(this.clock.instant()));
		return subpacketGenerator;
	}

	private void updateSignatureGenerator(InputStream source, PGPSignatureGenerator signatureGenerator)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		while ((bytesRead = source.read(buffer)) != -1) {
			signatureGenerator.update(buffer, 0, bytesRead);
		}
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}. The signing key may either contain a PHP private key block or
	 * reference a file.
	 * @param signingKey the signing key (either the key itself or a reference to a file)
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(String signingKey, String passphrase) throws IOException {
		return get(Clock.systemDefaultZone(), signingKey, passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}. The signing key may either contain a PHP private key block or
	 * reference a file.
	 * @param clock the clock to use when generating signatures
	 * @param signingKey the signing key (either the key itself or a reference to a file)
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(Clock clock, String signingKey, String passphrase) throws IOException {
		Assert.notNull(clock, "Clock must not be null");
		Assert.notNull(signingKey, "SigningKey must not be null");
		Assert.hasText(signingKey, "SigningKey must not be empty");
		if (isArmoredAscii(signingKey)) {
			byte[] bytes = signingKey.getBytes(StandardCharsets.UTF_8);
			return get(clock, new ByteArrayInputStream(bytes), passphrase);
		}
		Assert.isTrue(!signingKey.contains("\n"),
				"Signing key is not does not contain a PGP private key block and does not reference a file");
		return get(clock, new File(signingKey), passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} file and
	 * {@code passphrase}.
	 * @param signingKey the signing key file
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(File signingKey, String passphrase) throws IOException {
		return get(Clock.systemDefaultZone(), signingKey, passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}.
	 * @param clock the clock to use when generating signatures
	 * @param signingKey the signing key file
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(Clock clock, File signingKey, String passphrase) throws IOException {
		Assert.notNull(clock, "Clock must not be null");
		Assert.notNull(signingKey, "SigningKey must not be null");
		Assert.notNull(passphrase, "Passphrase must not be null");
		Assert.isTrue(signingKey.exists(), "Signing key file does not exist");
		Assert.isTrue(signingKey.isFile(), "Signing key file does not reference a file");
		return get(clock, new FileInputStream(signingKey), passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}.
	 * @param signingKey an {@link InputStreamSource} providing the signing key
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(InputStreamSource signingKey, String passphrase) throws IOException {
		return get(Clock.systemDefaultZone(), signingKey, passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}.
	 * @param clock the clock to use when generating signatures
	 * @param signingKey an {@link InputStreamSource} providing the signing key
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(Clock clock, InputStreamSource signingKey, String passphrase)
			throws IOException {
		Assert.notNull(clock, "Clock must not be null");
		Assert.notNull(signingKey, "SigningKey must not be null");
		Assert.notNull(passphrase, "Passphrase must not be null");
		return get(clock, signingKey.getInputStream(), passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}.
	 * @param signingKey an {@link InputStream} providing the signing key (will be closed
	 * after use)
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(InputStream signingKey, String passphrase) throws IOException {
		return get(Clock.systemDefaultZone(), signingKey, passphrase);
	}

	/**
	 * Get an {@link ArmoredAsciiSigner} for the given {@code signingKey} and
	 * {@code passphrase}.
	 * @param clock the clock to use when generating signatures
	 * @param signingKey an {@link InputStream} providing the signing key (will be closed
	 * after use)
	 * @param passphrase the passphrase to use
	 * @return an {@link ArmoredAsciiSigner} insance
	 * @throws IOException on IO error
	 */
	public static ArmoredAsciiSigner get(Clock clock, InputStream signingKey, String passphrase) throws IOException {
		Assert.notNull(clock, "Clock must not be null");
		Assert.notNull(signingKey, "SigningKey must not be null");
		Assert.notNull(passphrase, "Passphrase must not be null");
		return new ArmoredAsciiSigner(clock, signingKey, passphrase);
	}

	private static boolean isArmoredAscii(String signingKey) {
		return signingKey.contains(PRIVATE_KEY_BLOCK_HEADER);
	}

}
