/*
 * Copyright 2017-2018 the original author or authors.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * {@link Comparator} used to sort files returned from the {@link DirectoryScanner}.
 *
 * @author Phillip Webb
 */
final class FileComparator implements Comparator<File> {

	private final Map<File, String> roots;

	FileComparator(Map<File, String> roots) {
		this.roots = roots;
	}

	@Override
	public int compare(File file1, File file2) {
		Comparator<File> comparator = Comparator.comparing(File::getParentFile);
		comparator = comparator.thenComparingInt(this::byFileType);
		comparator = comparator.thenComparingInt(this::byPomExtension);
		comparator = comparator.thenComparing(this::getFileExtension);
		comparator = comparator.thenComparing(FileComparator::getNameWithoutExtension);
		return comparator.compare(file1, file2);
	}

	private int byFileType(File file) {
		if (isRoot(file)) {
			return 0;
		}
		if (isMavenMetaData(file)) {
			return 1;
		}
		return 2;
	}

	private int byPomExtension(File file) {
		return ("pom".equalsIgnoreCase(getFileExtension(file)) ? 1 : 0);
	}

	private String getFileExtension(File file) {
		return StringUtils.getFilenameExtension(file.getName());
	}

	private boolean isRoot(File file) {
		String root = this.roots.get(file.getParentFile());
		String name = getNameWithoutExtension(file);
		return (name != null && name.equals(root));
	}

	public static void sort(List<File> files) {
		MultiValueMap<File, File> filesByParent = getFilesByParent(files);
		Map<File, String> roots = getRoots(filesByParent);
		Collections.sort(files, new FileComparator(roots));
	}

	private static MultiValueMap<File, File> getFilesByParent(List<File> files) {
		MultiValueMap<File, File> filesByParent = new LinkedMultiValueMap<>();
		files.forEach((file) -> filesByParent.add(file.getParentFile(), file));
		return filesByParent;
	}

	private static Map<File, String> getRoots(MultiValueMap<File, File> filesByParent) {
		Map<File, String> roots = new LinkedHashMap<>();
		filesByParent.forEach((parent, files) -> {
			files.stream().filter(FileComparator::isRootCandidate)
					.map(FileComparator::getNameWithoutExtension)
					.reduce(FileComparator::getShortest)
					.ifPresent((root) -> roots.put(parent, root));
		});
		return roots;
	}

	private static boolean isRootCandidate(File file) {
		if (isMavenMetaData(file) || file.isHidden() || file.getName().startsWith(".")
				|| file.isDirectory() || isChecksumFile(file)) {
			return false;
		}
		return true;
	}

	private static boolean isMavenMetaData(File file) {
		return file.getName().toLowerCase().startsWith("maven-metadata.xml");
	}

	private static boolean isChecksumFile(File file) {
		String name = file.getName().toLowerCase();
		return name.endsWith(".md5") || name.endsWith("sha1");
	}

	private static String getNameWithoutExtension(File file) {
		String name = file.getName();
		String extension = StringUtils.getFilenameExtension(name);
		return (extension != null)
				? name.substring(0, name.length() - extension.length() - 1) : name;
	}

	private static String getShortest(String name1, String name2) {
		int len1 = (StringUtils.hasLength(name1)) ? name1.length() : Integer.MAX_VALUE;
		int len2 = (StringUtils.hasLength(name2)) ? name2.length() : Integer.MAX_VALUE;
		return (len1 < len2) ? name1 : name2;
	}

}
