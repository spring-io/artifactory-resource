/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.springframework.util.Assert;

/**
 * {@link TestRule} providing access to an artifactory server connection.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class ArtifactoryServerConnection implements TestRule {

	private Function<Artifactory, ArtifactoryServer> serverFactory;

	@Override
	public Statement apply(Statement base, Description description) {
		final DockerComposeRule rule = createRule();
		Statement statement = new Statement() {

			@Override
			public void evaluate() throws Throwable {
				ArtifactoryServerConnection.this.serverFactory = (
						artifactory) -> getArtifactoryServer(rule, artifactory);
				base.evaluate();
			}

		};
		return rule.apply(statement, description);
	}

	private DockerComposeRule createRule() {
		return DockerComposeRule.builder()
				.file("src/integration/resources/docker-compose.yml")
				.waitingForService("artifactory",
						HealthChecks.toRespond2xxOverHttp(8081,
								ArtifactoryServerConnection::artifactoryHealthUri))
				.build();
	}

	public ArtifactoryServer getArtifactoryServer(DockerComposeRule rule,
			Artifactory artifactory) {
		DockerPort port = rule.containers().container("artifactory").port(8081);
		return artifactory.server(artifactoryUri(port), "admin", "password");
	}

	private static String artifactoryUri(DockerPort port) {
		return port.inFormat("http://$HOST:$EXTERNAL_PORT/artifactory");
	}

	public ArtifactoryServer getArtifactoryServer(Artifactory artifactory) {
		Assert.state(this.serverFactory != null, "No artifactory server available");
		return this.serverFactory.apply(artifactory);
	}

	private static String artifactoryHealthUri(DockerPort port) {
		return artifactoryUri(port) + "/api/system/ping";
	}

}
