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
		String pipelineFile = parameters.getSingleValue(super.getOption(PIPELINE_FILE));

		LOGGER.info("Validating pipeline file: " + pipelineFile);

		File f = new File(pipelineFile);

		if (!f.exists()) {
			LOGGER.severe("Pipeline file not found: " + f);
			System.exit(1);
		}
		
		List<ValidationError> errors = new PipelineValidator(f).validate();
		logValidationErrors(errors);

		if (errors.stream().filter(error -> error.getType().isError())
			.count() > 0
		) {
			System.exit(1);
		} else {
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