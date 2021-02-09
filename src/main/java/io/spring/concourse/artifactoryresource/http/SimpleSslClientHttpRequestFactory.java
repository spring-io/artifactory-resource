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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * {@link SimpleClientHttpRequestFactory} with custom {@link SSLContext} support.
 *
 * @author Phillip Webb
 */
public class SimpleSslClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

	private final SSLContext sslContext;

	public SimpleSslClientHttpRequestFactory(SslContextFactory sslContextFactory) {
		try {
			this.sslContext = (sslContextFactory != null) ? sslContextFactory.getSslContext() : null;
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
		super.prepareConnection(connection, httpMethod);
		if (connection instanceof HttpsURLConnection) {
			prepareHttpsConnection((HttpsURLConnection) connection, httpMethod);
		}
	}

	private void prepareHttpsConnection(HttpsURLConnection connection, String httpMethod) {
		if (this.sslContext != null) {
			connection.setSSLSocketFactory(this.sslContext.getSocketFactory());
		}
	}

}
