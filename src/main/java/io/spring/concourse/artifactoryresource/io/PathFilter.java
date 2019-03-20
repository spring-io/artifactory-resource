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

package io.spring.concourse.artifactoryresource.io;

import java.util.List;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

/**
 * Filter that matches paths based on {@code include}/{@code exclude} patterns.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class PathFilter {

	private static final PathMatcher pathMatcher = new AntPathMatcher();

	private final List<String> include;

	private final List<String> exclude;

	public PathFilter(List<String> include, List<String> exclude) {
		Assert.notNull(include, "Include must not be null");
		Assert.notNull(exclude, "Exclude must not be null");
		this.include = include;
		this.exclude = exclude;
	}

	public boolean isMatch(String path) {
		return ((this.include.isEmpty() || hasMatch(pathMatcher, path, this.include))
				&& !hasMatch(pathMatcher, path, this.exclude));
	}

	private boolean hasMatch(PathMatcher pathMatcher, String path,
			List<String> patterns) {
		for (String pattern : patterns) {
			pattern = cleanPattern(path, pattern);
			if (pathMatcher.match(pattern, path)) {
				return true;
			}
		}
		return false;
	}

	private String cleanPattern(String path, String pattern) {
		if (path.startsWith("/")) {
			return !pattern.startsWith("/") ? "/" + pattern : pattern;
		}
		return pattern.startsWith("/") ? pattern.substring(1) : pattern;
	}

}
