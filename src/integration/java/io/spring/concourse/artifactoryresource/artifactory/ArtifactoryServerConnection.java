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

package io.spring.concourse.artifactoryresource.artifactory;

import java.util.function.Function;

import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * providing access to an artifactory server connection.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ArtifactoryServerConnection implements BeforeAllCallback, AfterAllCallback {

	private static final String SERVER_PROPERTY = "artifactoryServer";

	private Function<Artifactory, ArtifactoryServer> serverFactory;

	private Delegate delegate;

	private Delegate getDelegate() {
		String serverLocation = System.getProperty(SERVER_PROPERTY);
		if (StringUtils.hasLength(serverLocation) && !serverLocation.startsWith("$")) {
			return new RunningServerDelegate(serverLocation);
		}
		return new DockerDelegate();

	}

	public ArtifactoryServer getArtifactoryServer(Artifactory artifactory) {
		Assert.state(this.serverFactory != null, "No artifactory server available");
		return this.serverFactory.apply(artifactory);
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		before(context);
	}

	private void before(ExtensionContext context) throws Exception {
		this.delegate = getDelegate();
		this.serverFactory = (artifactory) -> (this.delegate).getArtifactoryServer(context, artifactory);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		this.delegate.after(context);
	}

	private interface Delegate {

		ArtifactoryServer getArtifactoryServer(ExtensionContext context, Artifactory artifactory);

		void after(ExtensionContext context);

	}

	private static class DockerDelegate implements Delegate {

		private final DockerComposeExtension extension;

		DockerDelegate() {
			this.extension = DockerComposeExtension.builder().file("src/integration/resources/docker-compose.yml")
					.waitingForService("artifactory",
							HealthChecks.toRespond2xxOverHttp(8081, DockerDelegate::artifactoryHealthUri))
					.build();
		}

		@Override
		public ArtifactoryServer getArtifactoryServer(ExtensionContext context, Artifactory artifactory) {
			DockerPort port = getDockerPort(context);
			return artifactory.server(artifactoryUri(port), "admin", "password", null);
		}

		private DockerPort getDockerPort(ExtensionContext context) {
			try {
				this.extension.beforeAll(context);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			return this.extension.containers().container("artifactory").port(8081);
		}

		@Override
		public void after(ExtensionContext context) {
			this.extension.afterAll(context);
		}

		private static String artifactoryHealthUri(DockerPort port) {
			return artifactoryUri(port) + "/api/system/ping";
		}

		private static String artifactoryUri(DockerPort port) {
			return port.inFormat("http://$HOST:$EXTERNAL_PORT/artifactory");
		}

	}

	private static class RunningServerDelegate implements Delegate {

		private final String uri;

		public RunningServerDelegate(String uri) {
			this.uri = uri;
		}

		@Override
		public ArtifactoryServer getArtifactoryServer(ExtensionContext extension, Artifactory artifactory) {
			return artifactory.server(this.uri, "admin", "password", null);
		}

		@Override
		public void after(ExtensionContext context) {
		}

	}

}
