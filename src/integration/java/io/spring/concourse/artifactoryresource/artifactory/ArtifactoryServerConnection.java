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

package io.spring.concourse.artifactoryresource.artifactory;

import java.util.function.Function;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link TestRule} providing access to an artifactory server connection.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ArtifactoryServerConnection implements TestRule {

	private static final String SERVER_PROPERTY = "artifactoryServer";

	private Function<Artifactory, ArtifactoryServer> serverFactory;

	@Override
	public Statement apply(Statement base, Description description) {
		return apply(base, description, getDelegate());
	}

	private Delegate<?> getDelegate() {
		String serverLocation = System.getProperty(SERVER_PROPERTY);
		if (StringUtils.hasLength(serverLocation) && !serverLocation.startsWith("$")) {
			return new RunningServerDelegate(serverLocation);
		}
		return new DockerDelegate();

	}

	private <R extends TestRule> Statement apply(Statement base, Description description,
			Delegate<R> delegate) {
		final R rule = delegate.createRule();
		Statement statement = new Statement() {

			@Override
			public void evaluate() throws Throwable {
				ArtifactoryServerConnection.this.serverFactory = (artifactory) -> delegate
						.getArtifactoryServer(rule, artifactory);
				base.evaluate();
			}

		};
		return rule.apply(statement, description);
	}

	public ArtifactoryServer getArtifactoryServer(Artifactory artifactory) {
		Assert.state(this.serverFactory != null, "No artifactory server available");
		return this.serverFactory.apply(artifactory);
	}

	private interface Delegate<R extends TestRule> {

		R createRule();

		ArtifactoryServer getArtifactoryServer(R rule, Artifactory artifactory);

	}

	private static class DockerDelegate implements Delegate<DockerComposeRule> {

		@Override
		public DockerComposeRule createRule() {
			return DockerComposeRule.builder()
					.file("src/integration/resources/docker-compose.yml")
					.waitingForService("artifactory", HealthChecks.toRespond2xxOverHttp(
							8081, DockerDelegate::artifactoryHealthUri))
					.build();
		}

		@Override
		public ArtifactoryServer getArtifactoryServer(DockerComposeRule rule,
				Artifactory artifactory) {
			DockerPort port = rule.containers().container("artifactory").port(8081);
			return artifactory.server(artifactoryUri(port), "admin", "password");
		}

		private static String artifactoryHealthUri(DockerPort port) {
			return artifactoryUri(port) + "/api/system/ping";
		}

		private static String artifactoryUri(DockerPort port) {
			return port.inFormat("http://$HOST:$EXTERNAL_PORT/artifactory");
		}

	}

	private static class RunningServerDelegate implements Delegate<TestRule> {

		private final String uri;

		public RunningServerDelegate(String uri) {
			this.uri = uri;
		}

		@Override
		public TestRule createRule() {
			return new ExternalResource() {

			};
		}

		@Override
		public ArtifactoryServer getArtifactoryServer(TestRule rule,
				Artifactory artifactory) {
			return artifactory.server(this.uri, "admin", "password");
		}

	}

}
