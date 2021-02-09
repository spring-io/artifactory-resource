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

import javax.net.ssl.SSLContext;

/**
 * Builds an {@link SSLContext} for use with an HTTP connection.
 *
 * @author Phillip Webb
 */
public interface SslContextFactory {

	/**
	 * Return a new {@link SSLContext} instance.
	 * @return the SSL context
	 * @throws GeneralSecurityException on security error
	 */
	SSLContext getSslContext() throws GeneralSecurityException;

}
