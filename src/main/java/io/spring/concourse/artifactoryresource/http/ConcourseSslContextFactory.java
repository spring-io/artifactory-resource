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

package io.spring.concourse.artifactoryresource.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * {@link SslContextFactory} that loads certificates propagated by Concourse.
 *
 * @author Phillip Webb
 * @see <a href=
 * "https://concourse-ci.org/implementing-resource-types.html#resource-certs">Certificate
 * Propagation</a>
 */
public class ConcourseSslContextFactory implements SslContextFactory {

	private static final Resource CA_CERTIFICATES_FILE = new FileSystemResource("/etc/ssl/certs/ca-certificates.crt");

	private final TrustManagerFactory trustManagerFactory;

	public ConcourseSslContextFactory() {
		this(CA_CERTIFICATES_FILE);
	}

	ConcourseSslContextFactory(Resource certificatesResource) {
		try {
			KeyStore keyStore = getKeyStore(certificatesResource);
			this.trustManagerFactory = getTrustStore(keyStore);
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private KeyStore getKeyStore(Resource certificatesResource) throws CertificateException, FileNotFoundException,
			KeyStoreException, IOException, NoSuchAlgorithmException {
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(null);
		List<Certificate> certificates = getCertificates(certificatesResource);
		for (int i = 0; i < certificates.size(); i++) {
			keyStore.setCertificateEntry("concourse-" + i, certificates.get(i));
		}
		return keyStore;
	}

	private List<Certificate> getCertificates(Resource certificatesResource) throws CertificateException, IOException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		try (InputStream inputStream = certificatesResource.getInputStream()) {
			return new ArrayList<>(certificateFactory.generateCertificates(inputStream));
		}
	}

	private TrustManagerFactory getTrustStore(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
			.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		return trustManagerFactory;
	}

	@Override
	public SSLContext getSslContext() throws GeneralSecurityException {
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, this.trustManagerFactory.getTrustManagers(), null);
		return sslContext;
	}

	public static boolean isAvailable() {
		return CA_CERTIFICATES_FILE.exists();
	}

}
