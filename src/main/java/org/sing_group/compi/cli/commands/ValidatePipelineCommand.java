package org.sing_group.compi.cli.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class ValidatePipelineCommand extends AbstractCommand {
	private static final Logger logger = LogManager.getLogger(ValidatePipelineCommand.class);

	@Override
	public void execute(final Parameters parameters) throws Exception {

		logger.info("Validating pipeline file: " + parameters.getSingleValue(super.getOption("p")));
		
		File f = new File(parameters.getSingleValueString(super.getOption("p")));
		
		if (!f.exists()) {
		  logger.error("Pipeline file not found: "+f);
		  System.exit(1);
		}
		
	  List<ValidationError> errors = new PipelineValidator(f).validate();
	  logValidationErrors(errors);
		
	  if (errors.stream().filter(error -> error.getType().isError()).count() > 0) {
	    System.exit(1);
	  } else {
	    System.exit(0);
	  }
	}

	/**
	 * Getter of the description
	 * 
	 * @return The description
	 */
	@Override
	public String getDescription() {
		return "Validates a pipeline";
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name
	 */
	@Override
	public String getName() {
		return "validate";
	}

	@Override
	public String getDescriptiveName() {
		return "Validate a pipeline";
	}

	/**
	 * Returns a {@link List} with all the {@link Option}
	 * 
	 * @return A {@link List} with all the {@link Option}
	 */
	@Override
	protected List<Option<?>> createOptions() {
		final List<Option<?>> options = new ArrayList<>();
		options.add(new StringOption("pipeline", "p", "pipeline file", false, true, false));
		return options;
	}
	
	 private void logValidationErrors(List<ValidationError> errors) {
	    errors.stream().forEach(error -> {
	      if (error.getType().isError()) {
	        logger.error(error.toString());
	      } else {
	        logger.warn(error.toString());
	      }
	    });
	  }

}