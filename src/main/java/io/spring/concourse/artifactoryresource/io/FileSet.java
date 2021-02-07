/*
 * Copyright 2017-2021 the original author or authors.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * An ordered set of files as returned from the {@link DirectoryScanner}. In order to
 * support correct timestamp generation when uploaded to Artifactory. Files in this set
 * are ordered as follows within each parent folder:
 * <ol>
 * <li>Primary artifacts (usually the JAR)</li>
 * <li>The POM</li>
 * <li>Maven metadata files</li>
 * <li>Additional artifacts (E.g. javadoc JARs)</li>
 * </ol>
 * <p>
 * In addition, files may be returned in uploaded batches if multi-threaded uploads are
 * being used.
 *
 * @author Philllip Webb
 */
public final class FileSet implements Iterable<File> {

	private final Map<File, String> roots;

	private final List<File> files;

	private FileSet(Map<File, String> roots, List<File> files) {
		this.roots = roots;
		this.files = Collections.unmodifiableList(files);
	}

	/**
	 * Return a new {@link FileSet} consisting of files from this set that match the given
	 * predicate.
	 * @param predicate the filter predicate
	 * @return a new filtered {@link FileSet} instance
	 */
	public FileSet filter(Predicate<File> predicate) {
		return new FileSet(this.roots, this.files.stream().filter(predicate).collect(Collectors.toList()));
	}

	/**
	 * Return the files from this set batched by {@link Category}. The contents of each
	 * batch can be safely uploaded in parallel.
	 * @return the batched files
	 */
	public MultiValueMap<Category, File> batchedByCategory() {
		MultiValueMap<Category, File> batched = new LinkedMultiValueMap<>();
		Arrays.stream(Category.values()).forEach((category) -> batched.put(category, new ArrayList<>()));
		this.files.forEach((file) -> batched.add(getCategory(this.roots, file), file));
		batched.entrySet().removeIf((entry) -> entry.getValue().isEmpty());
		return batched;
	}

	@Override
	public Iterator<File> iterator() {
		return this.files.iterator();
	}

	public static FileSet of(File... files) {
		Assert.notNull(files, "Files must not be null");
		return of(Arrays.asList(files));
	}

	public static FileSet of(List<File> files) {
		Assert.notNull(files, "Files must not be null");
		MultiValueMap<File, File> filesByParent = getFilesByParent(files);
		Map<File, String> roots = getRoots(filesByParent);
		Comparator<File> comparator = Comparator.comparing(File::getParent);
		comparator = comparator.thenComparing((file) -> getCategory(roots, file));
		comparator = comparator.thenComparing(FileSet::getFileExtension);
		comparator = comparator.thenComparing(FileSet::getNameWithoutExtension);
		List<File> sorted = new ArrayList<>(files);
		sorted.sort(comparator);
		return new FileSet(roots, sorted);
	}

	private static MultiValueMap<File, File> getFilesByParent(List<File> files) {
		MultiValueMap<File, File> filesByParent = new LinkedMultiValueMap<>();
		files.forEach((file) -> filesByParent.add(file.getParentFile(), file));
		return filesByParent;
	}

	private static Map<File, String> getRoots(MultiValueMap<File, File> filesByParent) {
		Map<File, String> roots = new LinkedHashMap<>();
		filesByParent.forEach((parent, files) -> findRoot(files).ifPresent((root) -> roots.put(parent, root)));
		return roots;
	}

	private static Optional<String> findRoot(List<File> files) {
		return files.stream().filter(FileSet::isRootCandidate).map(FileSet::getNameWithoutExtension)
				.reduce(FileSet::getShortest);
	}

	private static boolean isRootCandidate(File file) {
		if (isMavenMetaData(file) || file.isHidden() || file.getName().startsWith(".") || file.isDirectory()
				|| isChecksumFile(file)) {
			return false;
		}
		return true;
	}

	private static boolean isChecksumFile(File file) {
		String name = file.getName().toLowerCase();
		return name.endsWith(".md5") || name.endsWith("sha1");
	}

	private static String getShortest(String name1, String name2) {
		int len1 = (StringUtils.hasLength(name1)) ? name1.length() : Integer.MAX_VALUE;
		int len2 = (StringUtils.hasLength(name2)) ? name2.length() : Integer.MAX_VALUE;
		return (len1 < len2) ? name1 : name2;
	}

	private static Category getCategory(Map<File, String> roots, File file) {
		if (file.getName().endsWith(".pom")) {
			return Category.POM;
		}
		if (file.getName().endsWith(".asc")) {
			return Category.SIGNATURE;
		}
		if (isMavenMetaData(file)) {
			return Category.MAVEN_METADATA;
		}
		String root = roots.get(file.getParentFile());
		return getNameWithoutExtension(file).equals(root) ? Category.PRIMARY : Category.ADDITIONAL;
	}

	private static boolean isMavenMetaData(File file) {
		return file.getName().toLowerCase().startsWith("maven-metadata.xml")
				|| file.getName().toLowerCase().startsWith("maven-metadata-local.xml");
	}

	private static String getFileExtension(File file) {
		String extension = StringUtils.getFilenameExtension(file.getName());
		return (extension != null) ? extension : "";
	}

	private static String getNameWithoutExtension(File file) {
		String name = file.getName();
		String extension = StringUtils.getFilenameExtension(name);
		return (extension != null) ? name.substring(0, name.length() - extension.length() - 1) : name;
	}

	/**
	 * Categorization used for ordering and batching.
	 */
	public enum Category {

		/**
		 * The primary artifact (usually the JAR).
		 */
		PRIMARY("pimary"),

		/**
		 * The POM file.
		 */
		POM("pom file"),

		/**
		 * An ASC signature file.
		 */
		SIGNATURE("signature"),

		/**
		 * Maven metadata.
		 */
		MAVEN_METADATA("maven metadata"),

		/**
		 * Any artifacts that include a classifier (for example Source JARs).
		 */
		ADDITIONAL("additional");

		private final String description;

		Category(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return this.description;
		}

	}

}
