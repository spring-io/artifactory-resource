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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import io.spring.concourse.artifactoryresource.artifactory.payload.Checksums;
import io.spring.concourse.artifactoryresource.artifactory.payload.DeployableArtifact;
import io.spring.concourse.artifactoryresource.io.Checksum;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Default {@link ArtifactoryRepository} implementation communicating over HTTP.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class HttpArtifactoryRepository implements ArtifactoryRepository {

	private static final Object[] NO_VARIABLES = {};

	private static final int KB = 1024;

	private static final long CHECKSUM_THRESHOLD = 10 * KB;

	private final RestTemplate restTemplate;

	private final String uri;

	private final String repositoryName;

	public HttpArtifactoryRepository(RestTemplate restTemplate, String uri,
			String repositoryName) {
		this.restTemplate = restTemplate;
		this.uri = uri;
		this.repositoryName = repositoryName;
	}

	@Override
	public void deploy(DeployableArtifact artifact, DeployOption... options) {
		try {
			Assert.notNull(artifact, "Artifact must not be null");
			if (artifact.getSize() <= CHECKSUM_THRESHOLD || ObjectUtils
					.containsElement(options, DeployOption.DISABLE_CHECKSUM_UPLOADS)) {
				deployUsingContent(artifact);
				return;
			}
			try {
				deployUsingChecksum(artifact);
			}
			catch (HttpClientErrorException ex) {
				if (!ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
					throw ex;
				}
				deployUsingContent(artifact);
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void deployUsingChecksum(DeployableArtifact artifact) throws IOException {
		RequestEntity<Void> request = deployRequest(artifact)
				.header("X-Checksum-Deploy", "true").build();
		this.restTemplate.exchange(request, Void.class);
	}

	private void deployUsingContent(DeployableArtifact artifact) throws IOException {
		RequestEntity<Resource> request = deployRequest(artifact)
				.body(artifact.getContent());
		this.restTemplate.exchange(request, Void.class);
	}

	private BodyBuilder deployRequest(DeployableArtifact artifact) throws IOException {
		URI uri = UriComponentsBuilder.fromUriString(this.uri).path(this.repositoryName)
				.path(artifact.getPath())
				.path(buildMatrixParams(artifact.getProperties()))
				.buildAndExpand(NO_VARIABLES).encode().toUri();
		Checksums checksums = artifact.getChecksums();
		return RequestEntity.put(uri).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("X-Checksum-Sha1", checksums.getSha1())
				.header("X-Checksum-Md5", checksums.getMd5());
	}

	private String buildMatrixParams(Map<String, String> matrixParams)
			throws UnsupportedEncodingException {
		StringBuilder matrix = new StringBuilder();
		if (matrixParams != null && !matrixParams.isEmpty()) {
			for (Map.Entry<String, String> entry : matrixParams.entrySet()) {
				matrix.append(";" + entry.getKey() + "=" + entry.getValue());
			}
		}
		return matrix.toString();
	}

	@Override
	public void download(String path, File destination, boolean downloadChecksums) {
		Assert.hasLength(path, "Path must not be empty");
		getFile(path, destination);
		if (downloadChecksums && !Checksum.isChecksumFile(path)) {
			Checksum.getFileExtensions().forEach((checksumExtension) -> {
				try {
					getFile(path + checksumExtension, destination);
				}
				catch (HttpClientErrorException ex) {
					// Ignore checksum download failures
				}
			});
		}
	}

	private void getFile(String path, File destination) {
		URI uri = UriComponentsBuilder.fromUriString(this.uri).path(this.repositoryName)
				.path("/" + path).buildAndExpand(NO_VARIABLES).encode().toUri();
		this.restTemplate.execute(uri, HttpMethod.GET, null,
				getResponseExtractor(path, destination));
	}

	private ResponseExtractor<Void> getResponseExtractor(String path, File destination) {
		return (response) -> {
			Path fullPath = destination.toPath().resolve(path);
			Files.createDirectories(fullPath.getParent());
			Files.copy(response.getBody(), fullPath);
			return null;
		};
	}

}
