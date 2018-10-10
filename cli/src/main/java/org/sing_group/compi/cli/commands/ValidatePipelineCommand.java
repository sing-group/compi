/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class ValidatePipelineCommand extends AbstractCommand {
	private static final Logger LOGGER = getLogger(ValidatePipelineCommand.class.getName());

	private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
	private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
	private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;

	@Override
	public void execute(final Parameters parameters) throws Exception {
		String pipelineNameFile = parameters.getSingleValue(super.getOption(PIPELINE_FILE));

		LOGGER.info("Validating pipeline file: " + pipelineNameFile);

		File pipelineFile = new File(pipelineNameFile);

		if (!pipelineFile.exists()) {
			throw new IllegalArgumentException(
				"Pipeline file not found: " + pipelineNameFile);
		}
		
		List<ValidationError> errors = new PipelineValidator(pipelineFile).validate();
		logValidationErrors(errors);

		if (errors.stream().filter(error -> error.getType().isError())
			.count() > 0
		) {
			System.exit(1);
		} else {
			LOGGER.info("Pipeline file is OK.");
			System.exit(0);
		}
	}

	@Override
	public String getDescription() {
		return "Validates a pipeline.";
	}

	@Override
	public String getName() {
		return "validate";
	}

	@Override
	public String getDescriptiveName() {
		return "Validate a pipeline";
	}

	@Override
	protected List<Option<?>> createOptions() {
		final List<Option<?>> options = new ArrayList<>();
		options.add(getPipelineOption());

		return options;
	}

	private Option<?> getPipelineOption() {
		return new StringOption(PIPELINE_FILE_LONG, PIPELINE_FILE,
			PIPELINE_FILE_DESCRIPTION, false, true, false);
	}

	private void logValidationErrors(List<ValidationError> errors) {
		errors.stream().forEach(error -> {
			if (error.getType().isError()) {
				LOGGER.severe(error.toString());
			} else {
				LOGGER.warning(error.toString());
			}
		});
	}

}
