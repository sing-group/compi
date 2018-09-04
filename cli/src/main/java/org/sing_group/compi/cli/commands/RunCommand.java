package org.sing_group.compi.cli.commands;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static org.sing_group.compi.cli.PipelineCLIApplication.newPipelineCLIApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.VariableResolver;
import org.sing_group.compi.core.validation.ValidationError;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.IntegerDefaultValuedStringConstructedOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunCommand extends AbstractCommand {
	private static final String ARGS_DELIMITER = "--";

	private static final Logger LOGGER = Logger.getLogger(RunCommand.class.getName());

	private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
	private static final String PARAMS_FILE = "pa";
	private static final String NUM_THREADS = "t";
	private static final String SKIP = "s";
	private static final String SINGLE_TASK = "st";
	private static final String RUNNERS_CONFIG_FILE = "r";
	
	private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
	private static final String PARAMS_FILE_LONG = "params";
	private static final String NUM_THREADS_LONG = "num-threads";
	private static final String SKIP_LONG = "skip";
	private static final String SINGLE_TASK_LONG = "single-task";
	private static final String RUNNERS_CONFIG_FILE_LONG = "runners-config";

	private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;
	private static final String PARAMS_FILE_DESCRIPTION = "XML params file";
	private static final String NUM_THEADS_DESCRIPTION = "number of threads to use";
	private static final String SKIP_DESCRIPTION = "skip to task. Runs the "
		+ "pipeline from the specific without running its dependencies. This "
		+ "option is incompatible with --" + SINGLE_TASK_LONG;
	private static final String SINGLE_TASK_DESCRIPTION = "Runs a single task "
		+ "without its depencendies. This option is incompatible with --"
		+ SKIP_LONG;
	private static final String RUNNERS_CONFIG_DESCRIPTION = "XML file configuring custom runners for tasks. See the "
	  + "Compi documentation for more details";

	private static final String NUM_THREADS_DEFAULT = "6";

	private String[] commandLineArgs;
	private CompiApp compi;

	public RunCommand(String[] commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}

	@Override
	public void execute(final Parameters parameters) throws Exception {
		String pipelineFile = parameters.getSingleValueString(super.getOption(PIPELINE_FILE));
		Integer threads = parameters.getSingleValue(super.getOption(NUM_THREADS));

		LOGGER.info("Compi running with: ");
		LOGGER.info("Pipeline file - " + pipelineFile);
		LOGGER.info("Number of threads - " + threads);
		
		if (parameters.hasOption(super.getOption(PARAMS_FILE))) {
			LOGGER.info("Params file - " + parameters.getSingleValue(super.getOption(PARAMS_FILE)));
		}

		boolean hasSkip = parameters.hasOption(super.getOption(SKIP));
		boolean hasSingleTask = parameters.hasOption(super.getOption(SINGLE_TASK));

		if (hasSkip && hasSingleTask) {
			throw new IllegalArgumentException(
				"You can specify skip or single-task, but not both at the same time.");
		}

		String skip = hasSkip
			? parameters.getSingleValueString(super.getOption(SKIP))
			: null;
		String singleTask = hasSingleTask
			? parameters.getSingleValueString(super.getOption(SINGLE_TASK))
			: null;

		if (skip != null) {
			LOGGER.info("Skip to task - " + skip + "\n");
		} else if (singleTask != null) {
			LOGGER.info("Running single task - " + singleTask + "\n");
		}

		try {
			List<ValidationError> errors = new ArrayList<>();
			compi = new CompiApp(
				pipelineFile, threads, (VariableResolver) null, 
				skip, singleTask, errors
			);
			logValidationErrors(errors);
			
			if (parameters.hasOption(super.getOption(RUNNERS_CONFIG_FILE))) {
			  File runnersFile = new File(parameters.getSingleValueString(super.getOption(RUNNERS_CONFIG_FILE)));
			  if (!runnersFile.exists()) {
			    throw new IllegalArgumentException("The runners file does not exist: "+runnersFile);
			  }
			  compi.setRunnersConfiguration(runnersFile);
			}
			
			CLIApplication pipelineApplication = newPipelineCLIApplication(
				pipelineFile, compi, this.createOptions(), this.commandLineArgs);
			
			pipelineApplication.run(getPipelineParameters(this.commandLineArgs));

		} catch (PipelineValidationException e) {
			LOGGER.severe("Pipeline is not valid");
			logValidationErrors(e.getErrors());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			LOGGER.severe(e.getClass() + ": " + e.getMessage());
		}
	}

	@Override
	public String getDescription() {
		return "Runs a pipeline.";
	}

	@Override
	public String getName() {
		return "run";
	}

	@Override
	public String getDescriptiveName() {
		return "Run compi";
	}

	@Override
	protected List<Option<?>> createOptions() {
		final List<Option<?>> options = new ArrayList<>();
		options.add(getPipelineFile());
		options.add(getParamsFile());
		options.add(getNumThreads());
		options.add(getSkipToTask());
		options.add(getRunSingleTask());
		options.add(getRunnersConfigFile());

		return options;
	}

	private Option<?> getPipelineFile() {
		return new StringOption(PIPELINE_FILE_LONG, PIPELINE_FILE,
			PIPELINE_FILE_DESCRIPTION, false, true, false);
	}

	private Option<?> getParamsFile() {
		return new StringOption(PARAMS_FILE_LONG, PARAMS_FILE,
			PARAMS_FILE_DESCRIPTION, true, true, false);
	}

	private Option<?> getNumThreads() {
		return new IntegerDefaultValuedStringConstructedOption(NUM_THREADS_LONG,
			NUM_THREADS, NUM_THEADS_DESCRIPTION, NUM_THREADS_DEFAULT);
	}

	private Option<?> getSkipToTask() {
		return new StringOption(SKIP_LONG, SKIP, SKIP_DESCRIPTION, true, true);
	}

	private Option<?> getRunSingleTask() {
		return new StringOption(SINGLE_TASK_LONG, SINGLE_TASK,
			SINGLE_TASK_DESCRIPTION, true, true);
	}

	private Option<?> getRunnersConfigFile() {
    return new StringOption(RUNNERS_CONFIG_FILE_LONG, RUNNERS_CONFIG_FILE,
      RUNNERS_CONFIG_DESCRIPTION, true, true, false);
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

	private static String[] getPipelineParameters(String[] args) {
		int paramsDelimiterIndex = asList(args).indexOf(ARGS_DELIMITER);

		// after --
		String[] pipelineParameters = new String[] { "run" };
		if (paramsDelimiterIndex > 0
			&& paramsDelimiterIndex < args.length - 1) {
			pipelineParameters = new String[args.length - paramsDelimiterIndex
				- 1 + 1];
			pipelineParameters[0] = "run";
			arraycopy(args, paramsDelimiterIndex + 1, pipelineParameters, 1,
				pipelineParameters.length - 1);
		}

		return pipelineParameters;
	}

	public static String[] getCompiParameters(String[] args) {
		int paramsDelimiterIndex = asList(args).indexOf(ARGS_DELIMITER);

		// before --
		String[] compiParameters = args;
		if (paramsDelimiterIndex > 0) {
			compiParameters = new String[paramsDelimiterIndex];
			arraycopy(args, 0, compiParameters, 0, paramsDelimiterIndex);
		}

		return compiParameters;
	}
}
