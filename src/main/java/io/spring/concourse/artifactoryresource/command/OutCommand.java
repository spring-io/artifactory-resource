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

package io.spring.concourse.artifactoryresource.command;

import io.spring.concourse.artifactoryresource.command.payload.OutRequest;
import io.spring.concourse.artifactoryresource.command.payload.OutResponse;
import io.spring.concourse.artifactoryresource.io.Directory;
import io.spring.concourse.artifactoryresource.system.SystemInput;
import io.spring.concourse.artifactoryresource.system.SystemOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * Command to handle requests to the {@code "/opt/resource/out"} script.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
@Component
public class OutCommand implements Command {

	private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

	private final SystemInput systemInput;

	private final SystemOutput systemOutput;

	private final OutHandler handler;

	public OutCommand(SystemInput systemInput, SystemOutput systemOutput, OutHandler handler) {
		this.systemInput = systemInput;
		this.systemOutput = systemOutput;
		this.handler = handler;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.debug("Running '/out' command");
		OutRequest request = this.systemInput.read(OutRequest.class);
		Directory directory = Directory.fromArgs(args);
		OutResponse response = this.handler.handle(request, directory);
		logger.trace("Writing response {}", response);
		this.systemOutput.write(response);
	}

}
