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

package io.spring.concourse.artifactoryresource.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;

import org.springframework.util.StringUtils;

/**
 * {@link Comparator} used to sort files returned from the {@link DirectoryScanner}.
 *
 * @author Phillip Webb
 */
final class FileComparator {

	static Comparator<File> INSTANCE = Comparator.comparing(FileComparator::parentPath)
			.thenComparing(FileComparator::extension)
			.thenComparing(FileComparator::fileNameWithoutExtension);

	private FileComparator() {
	}

	static Path parentPath(File file) {
		return file.getParentFile().toPath();
	}

	static int extension(File file1, File file2) {
		String extension1 = StringUtils.getFilenameExtension(file1.getName());
		String extension2 = StringUtils.getFilenameExtension(file2.getName());
		extension1 = (extension1 == null ? "" : extension1);
		extension2 = (extension1 == null ? "" : extension2);
		if (extension1.equals(extension2)) {
			return 0;
		}
		if ("pom".equals(extension1)) {
			return -1;
		}
		if ("pom".equals(extension2)) {
			return 1;
		}
		return extension1.compareTo(extension2);
	}

	static String fileNameWithoutExtension(File file) {
		String name = file.getName();
		String extension = StringUtils.getFilenameExtension(name);
		if (StringUtils.hasLength(extension)) {
			return name.substring(0, name.length() - extension.length() - 1);
		}
		return name;
	}

}
