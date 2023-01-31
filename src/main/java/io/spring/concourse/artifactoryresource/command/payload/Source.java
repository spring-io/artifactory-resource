/*
 * Copyright 2017-2023 the original author or authors.
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

package io.spring.concourse.artifactoryresource.command.payload;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The source payload containing shared configuration.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 * @author Gabriel Petrovay
 */
public class Source {

	private final String uri;

	private final String username;

	private final String password;

	private final String buildName;

	private final Integer checkLimit;

	@JsonIgnore
	private final Proxy proxy;

	@JsonCreator
	public Source(@JsonProperty("uri") String uri, @JsonProperty("username") String username,
			@JsonProperty("password") String password, @JsonProperty("build_name") String buildName,
			@JsonProperty("check_limit") Integer checkLimit, @JsonProperty("proxy_host") String proxyHost,
			@JsonProperty("proxy_port") Integer proxyPort) {
		Assert.hasText(uri, "URI must not be empty");
		Assert.hasText(buildName, "Build Name must not be empty");
		this.uri = uri;
		this.username = username;
		this.password = password;
		this.buildName = buildName;
		this.checkLimit = checkLimit;
		this.proxy = (StringUtils.hasText(proxyHost)) ? createProxy(proxyHost, proxyPort) : null;
	}

	private Proxy createProxy(String host, Integer port) {
		Assert.notNull(port, "Proxy port must be provided");
		return new Proxy(Type.HTTP, new InetSocketAddress(host, port));
	}

	public String getUri() {
		return this.uri;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public String getBuildName() {
		return this.buildName;
	}

	public Integer getCheckLimit() {
		return this.checkLimit;
	}

	public Proxy getProxy() {
		return this.proxy;
	}

	@Override
	public String toString() {
		ToStringCreator creator = new ToStringCreator(this).append("uri", this.uri).append("buildName", this.buildName)
				.append("checkLimit", this.checkLimit);
		if (this.proxy != null) {
			creator.append("proxy", this.proxy);
		}
		return creator.toString();
	}

}
