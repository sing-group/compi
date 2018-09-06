package org.sing_group.compi.cli.commands;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.cli.PipelineCLIApplication.newPipelineCLIApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private static final String NUM_PARALLEL_TASKS = "n";
  private static final String FROM = "f";
  private static final String AFTER = "a";
  private static final String SINGLE_TASK = "st";
  private static final String UNTIL_TASK = "ut";
  private static final String BEFORE_TASK = "bt";
  private static final String RUNNERS_CONFIG_FILE = "r";

  private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
  private static final String PARAMS_FILE_LONG = "params";
  private static final String NUM_PARALLEL_TASKS_LONG = "num-tasks";
  private static final String FROM_LONG = "from";
  private static final String AFTER_LONG = "after";
  private static final String SINGLE_TASK_LONG = "single-task";
  private static final String UNTIL_TASK_LONG = "until";
  private static final String BEFORE_TASK_LONG = "before";
  private static final String RUNNERS_CONFIG_FILE_LONG = "runners-config";

  private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;
  private static final String PARAMS_FILE_DESCRIPTION = "XML parameters file";
  private static final String NUM_PARALLEL_TASKS_DESCRIPTION =
    "maximum number of tasks that can be run in parallel. This is not equivalent to the number of threads the pipeline "
      + "will use, because some tasks can be parallel processes themselves";
  private static final String FROM_DESCRIPTION =
    "from task(s). Runs the "
      + "pipeline from the specific task(s) without running its/their dependencies. This option is incompatible with --"
      + SINGLE_TASK_LONG;
  private static final String AFTER_DESCRIPTION =
    "after task(s). Runs the "
      + "pipeline from the specific task(s) without running neither it/them nor its/their dependencies. This option "
      + "is incompatible with --" + SINGLE_TASK_LONG;
  private static final String SINGLE_TASK_DESCRIPTION =
    "runs a single task "
      + "without its depencendies. This option is incompatible with --" + FROM_LONG + ", --" + AFTER_LONG + 
      ", --" + UNTIL_TASK_LONG + " and --" + BEFORE_TASK_LONG;
  private static final String UNTIL_TASK_DESCRIPTION =
    "runs until a task (inclusive) "
      + "including its depencendies. This option is incompatible with --"
      + SINGLE_TASK_LONG + " and --" + BEFORE_TASK_LONG;
  private static final String BEFORE_TASK_DESCRIPTION =
    "runs all tasks which are dependencies of a given task. "
      + "This option is incompatible with --"
      + SINGLE_TASK_LONG + " and --" + UNTIL_TASK_LONG;
  private static final String RUNNERS_CONFIG_DESCRIPTION =
    "XML file configuring custom runners for tasks. See the "
      + "Compi documentation for more details";

  private static final String DEFAULT_NUM_PARALLEL_TASKS = "6";

  private String[] commandLineArgs;
  private CompiApp compi;

  public RunCommand(String[] commandLineArgs) {
    this.commandLineArgs = commandLineArgs;
  }

  @Override
  public void execute(final Parameters parameters) throws Exception {
    String pipelineFile = parameters.getSingleValueString(super.getOption(PIPELINE_FILE));
    Integer compiThreads = parameters.getSingleValue(super.getOption(NUM_PARALLEL_TASKS));

    boolean hasFrom = parameters.hasOption(super.getOption(FROM));
    boolean hasAfter = parameters.hasOption(super.getOption(AFTER));
    boolean hasSingleTask = parameters.hasOption(super.getOption(SINGLE_TASK));
    boolean hasUntilTask = parameters.hasOption(super.getOption(UNTIL_TASK));
    boolean hasBeforeTask = parameters.hasOption(super.getOption(BEFORE_TASK));

    if (hasSingleTask && (hasFrom || hasAfter || hasUntilTask || hasBeforeTask)) {
      throw new IllegalArgumentException(
        "--" + SINGLE_TASK_LONG + " is incompatible with any of --" + FROM_LONG + ", --" + AFTER_LONG + ", --"
          + UNTIL_TASK_LONG + ", and "
          + BEFORE_TASK_LONG
      );
    }
    if (hasUntilTask && hasBeforeTask) {
      throw new IllegalArgumentException("--" + UNTIL_TASK_LONG + " is incompatible with --" + BEFORE_TASK_LONG);
    }

    List<String> fromTasks =
      hasFrom
        ? parameters.getAllValuesString(super.getOption(FROM))
        : null;
    List<String> afterTasks =
      hasAfter
        ? parameters.getAllValuesString(super.getOption(AFTER))
        : null;

    if (hasAfter && hasFrom) {
      Set<String> afterTasksSet = new HashSet<>(afterTasks);
      afterTasksSet.retainAll(new HashSet<>(fromTasks));
      if (afterTasksSet.size() > 0) {
        throw new IllegalArgumentException(
          "--" + FROM_LONG + " and --" + AFTER_LONG + "  have tasks in common, which is illegal. Common tasks: "
            + afterTasksSet.stream().collect(joining(", "))
        );
      }
    }

    String singleTask =
      hasSingleTask
        ? parameters.getSingleValueString(super.getOption(SINGLE_TASK))
        : null;
    String untilTask =
      hasUntilTask
        ? parameters.getSingleValueString(super.getOption(UNTIL_TASK))
        : null;
    String beforeTask =
      hasBeforeTask
        ? parameters.getSingleValueString(super.getOption(BEFORE_TASK))
        : null;

    LOGGER.info("Compi running with: ");
    LOGGER.info("Pipeline file - " + pipelineFile);
    LOGGER.info("Max number of parallel tasks - " + compiThreads);

    if (parameters.hasOption(super.getOption(PARAMS_FILE))) {
      LOGGER.info("Params file - " + parameters.getSingleValue(super.getOption(PARAMS_FILE)));
    }

    if (singleTask != null) {
      LOGGER.info("Running single task - " + singleTask);
    }

    if (fromTasks != null) {
      LOGGER.info("Running from task(s) - " + fromTasks.stream().collect(joining(", ")));
    }
    if (afterTasks != null) {
      LOGGER.info("Running after task(s) - " + afterTasks.stream().collect(joining(", ")));
    }

    if (untilTask != null) {
      LOGGER.info("Running until task - " + untilTask);
    }
    if (beforeTask != null) {
      LOGGER.info("Running tasks before task - " + beforeTask);
    }

    try {
      List<ValidationError> errors = new ArrayList<>();
      compi =
        new CompiApp(
          pipelineFile, compiThreads, (VariableResolver) null,
          singleTask, fromTasks, afterTasks, untilTask, beforeTask, errors
        );
      logValidationErrors(errors);

      if (parameters.hasOption(super.getOption(RUNNERS_CONFIG_FILE))) {
        File runnersFile = new File(parameters.getSingleValueString(super.getOption(RUNNERS_CONFIG_FILE)));
        if (!runnersFile.exists()) {
          throw new IllegalArgumentException("The runners file does not exist: " + runnersFile);
        }
        LOGGER.info("Runners file - " + runnersFile);
        compi.setRunnersConfiguration(runnersFile);
      }

      CLIApplication pipelineApplication =
        newPipelineCLIApplication(
          pipelineFile, compi, this.createOptions(), this.commandLineArgs
        );

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
    options.add(getNumParallelTasks());
    options.add(getRunSingleTask());
    options.add(getRunFromTasks());
    options.add(getRunAfterTasks());
    options.add(getRunUntilTask());
    options.add(getRunBeforeTask());
    options.add(getRunnersConfigFile());

    return options;
  }

  private Option<?> getPipelineFile() {
    return new StringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE,
      PIPELINE_FILE_DESCRIPTION, false, true, false
    );
  }

  private Option<?> getParamsFile() {
    return new StringOption(
      PARAMS_FILE_LONG, PARAMS_FILE,
      PARAMS_FILE_DESCRIPTION, true, true, false
    );
  }

  private Option<?> getNumParallelTasks() {
    return new IntegerDefaultValuedStringConstructedOption(
      NUM_PARALLEL_TASKS_LONG,
      NUM_PARALLEL_TASKS, NUM_PARALLEL_TASKS_DESCRIPTION, DEFAULT_NUM_PARALLEL_TASKS
    );
  }

  private Option<?> getRunFromTasks() {
    return new StringOption(FROM_LONG, FROM, FROM_DESCRIPTION, true, true, true);
  }

  private Option<?> getRunAfterTasks() {
    return new StringOption(AFTER_LONG, AFTER, AFTER_DESCRIPTION, true, true, true);
  }

  private Option<?> getRunSingleTask() {
    return new StringOption(
      SINGLE_TASK_LONG, SINGLE_TASK,
      SINGLE_TASK_DESCRIPTION, true, true
    );
  }

  private Option<?> getRunUntilTask() {
    return new StringOption(
      UNTIL_TASK_LONG, UNTIL_TASK,
      UNTIL_TASK_DESCRIPTION, true, true
    );
  }

  private Option<?> getRunBeforeTask() {
    return new StringOption(
      BEFORE_TASK_LONG, BEFORE_TASK,
      BEFORE_TASK_DESCRIPTION, true, true
    );
  }

  private Option<?> getRunnersConfigFile() {
    return new StringOption(
      RUNNERS_CONFIG_FILE_LONG, RUNNERS_CONFIG_FILE,
      RUNNERS_CONFIG_DESCRIPTION, true, true, false
    );
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
    String[] pipelineParameters = new String[] {
      "run"
    };
    if (
      paramsDelimiterIndex > 0
        && paramsDelimiterIndex < args.length - 1
    ) {
      pipelineParameters =
        new String[args.length - paramsDelimiterIndex
          - 1 + 1];
      pipelineParameters[0] = "run";
      arraycopy(
        args, paramsDelimiterIndex + 1, pipelineParameters, 1,
        pipelineParameters.length - 1
      );
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
