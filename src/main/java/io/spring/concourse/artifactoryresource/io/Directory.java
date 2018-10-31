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
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A directory passed to a command.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
public class Directory {

	private static final File NO_FILE = null;

	private final File file;

	public Directory(String path) {
		this((path != null) ? new File(path) : NO_FILE);
	}

	public Directory(File file) {
		Assert.notNull(file, "File must not be null");
		Assert.state(file.exists(), "File '" + file + "' does not exist");
		Assert.state(file.isDirectory(), "File '" + file + "' is not a directory");
		this.file = file;
	}

	public File getFile() {
		return this.file;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return this.file.equals(((Directory) obj).file);
	}

	@Override
	public int hashCode() {
		return this.file.hashCode();
	}

	@Override
	public String toString() {
		return StringUtils.cleanPath(this.file.getPath());
	}

	public Directory getSubDirectory(String path) {
		if (StringUtils.hasText(path)) {
			return new Directory(new File(this.file, StringUtils.cleanPath(path)));
		}
		return this;
	}

	public static Directory fromArgs(ApplicationArguments args) {
		List<String> nonOptionArgs = args.getNonOptionArgs();
		Assert.state(nonOptionArgs.size() >= 2, "No directory argument specified");
		return new Directory(nonOptionArgs.get(1));
	}

}
