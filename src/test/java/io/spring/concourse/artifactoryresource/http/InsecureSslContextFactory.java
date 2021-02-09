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

package io.spring.concourse.artifactoryresource.http;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * {@link SslContextFactory} returning a context that blindly trusts all connections.
 *
 * @author Phillip Webb
 */
public class InsecureSslContextFactory implements SslContextFactory {

	private static final TrustManager[] TRUST_MANAGERS = new InsecureTrustManager[] { new InsecureTrustManager() };

	@Override
	public SSLContext getSslContext() throws GeneralSecurityException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, TRUST_MANAGERS, new SecureRandom());
		return sslContext;
	}

	public static class InsecureTrustManager implements X509TrustManager {

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

	}

}
