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

package io.spring.concourse.artifactoryresource.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility to scan a {@link Directory} for contents.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class DirectoryScanner {

	/**
	 * Scan the given directory for files, accounting for the include and exclude
	 * patterns.
	 * @param directory the source directory
	 * @param include the include patterns
	 * @return the scanned list of files
	 */
	public FileSet scan(Directory directory, List<String> include) {
		return this.scan(directory, include, Collections.emptyList());
	}

	/**
	 * Scan the given directory for files, accounting for the include and exclude
	 * patterns.
	 * @param directory the source directory
	 * @param include the include patterns
	 * @param exclude the exclude patterns
	 * @return the scanned list of files
	 */
	public FileSet scan(Directory directory, List<String> include, List<String> exclude) {
		try {
			Path path = directory.getFile().toPath();
			return FileSet.of(Files.find(path, Integer.MAX_VALUE, getFilter(directory, include, exclude))
				.map(Path::toFile)
				.toArray(File[]::new));
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private BiPredicate<Path, BasicFileAttributes> getFilter(Directory root, List<String> include,
			List<String> exclude) {
		PathFilter filter = new PathFilter(include, exclude);
		String rootPath = StringUtils.cleanPath(root.getFile().getPath());
		return (path, fileAttributes) -> {
			if (!path.toFile().isFile()) {
				return false;
			}
			String relativePath = StringUtils.cleanPath(path.toString()).substring(rootPath.length() + 1);
			return filter.isMatch(relativePath);
		};
	}

}
