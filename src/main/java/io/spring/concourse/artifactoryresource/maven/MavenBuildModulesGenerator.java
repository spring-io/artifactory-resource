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

package io.spring.concourse.artifactoryresource.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.concourse.artifactoryresource.artifactory.BuildModulesGenerator;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildArtifact;
import io.spring.concourse.artifactoryresource.artifactory.payload.BuildModule;
import io.spring.concourse.artifactoryresource.artifactory.payload.Checksums;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * {@link BuildModulesGenerator} that using standard Maven layout rules.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class MavenBuildModulesGenerator implements BuildModulesGenerator {

	private static final Map<String, String> SUFFIX_TYPES;

	static {
		Map<String, String> suffixTypes = new LinkedHashMap<>();
		suffixTypes.put("-sources.jar", "java-source-jar");
		SUFFIX_TYPES = Collections.unmodifiableMap(suffixTypes);
	}

	private static final Set<String> IGNORED = Collections
		.unmodifiableSet(new LinkedHashSet<>(Arrays.asList("md5", "sha")));

	private static Pattern PATH_PATTERN = Pattern.compile("\\/(.*)\\/(.*)\\/(.*)\\/(.*)");

	@Override
	public List<BuildModule> getBuildModules(List<DeployableArtifact> deployableArtifacts) {
		List<BuildModule> buildModules = new ArrayList<>();
		getBuildArtifactsById(deployableArtifacts)
			.forEach((id, artifacts) -> buildModules.add(new BuildModule(id, artifacts)));
		return buildModules;
	}

	private MultiValueMap<String, BuildArtifact> getBuildArtifactsById(List<DeployableArtifact> deployableArtifacts) {
		MultiValueMap<String, BuildArtifact> buildArtifacts = new LinkedMultiValueMap<>();
		deployableArtifacts.forEach((deployableArtifact) -> {
			try {
				String id = getArtifactId(deployableArtifact);
				getBuildArtifact(deployableArtifact)
					.ifPresent((buildArtifact) -> buildArtifacts.add(id, buildArtifact));
			}
			catch (Exception ex) {
				// Ignore and don't add as a module
			}
		});
		return buildArtifacts;
	}

	private String getArtifactId(DeployableArtifact deployableArtifact) {
		Matcher pathMatcher = getPathMatcher(deployableArtifact);
		String groupId = pathMatcher.group(1).replace('/', '.');
		String artifactId = pathMatcher.group(2);
		String version = pathMatcher.group(3);
		return groupId + ":" + artifactId + ":" + version;
	}

	private Optional<BuildArtifact> getBuildArtifact(DeployableArtifact deployableArtifact) {
		Matcher pathMatcher = getPathMatcher(deployableArtifact);
		String filename = pathMatcher.group(4);
		String type = getType(filename);
		if (type == null) {
			return Optional.empty();
		}
		Checksums checksums = deployableArtifact.getChecksums();
		return Optional.of(new BuildArtifact(type, checksums.getSha1(), checksums.getMd5(), filename));
	}

	private String getType(String name) {
		for (Map.Entry<String, String> entry : SUFFIX_TYPES.entrySet()) {
			if (name.toLowerCase().endsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		String extension = StringUtils.getFilenameExtension(name).toLowerCase();
		if (extension == null || IGNORED.contains(extension)) {
			return null;
		}
		return extension;
	}

	private Matcher getPathMatcher(DeployableArtifact deployableArtifact) {
		String path = deployableArtifact.getPath();
		Matcher matcher = PATH_PATTERN.matcher(path);
		Assert.state(matcher.matches(), "Invalid path " + path);
		return matcher;
	}

}
