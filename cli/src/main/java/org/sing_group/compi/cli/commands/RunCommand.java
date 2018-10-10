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

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.cli.PipelineCLIApplication.newPipelineCLIApplication;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.xmlio.entities.Pipeline.fromFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiRunConfiguration;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.validation.ValidationError;
import org.sing_group.compi.xmlio.entities.Pipeline;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
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
  private static final String LOGS_DIR = "l";
  private static final String LOG_ONLY_TASK = "lt";
  private static final String LOG_EXCLUDE_TASK = "nl";
  private static final String SHOW_STD_OUTS = "o";
  private static final String QUIET = "q";
  private static final String ABORT_IF_WARNINGS = "w";

  private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
  private static final String PARAMS_FILE_LONG = "params";
  private static final String NUM_PARALLEL_TASKS_LONG = "num-tasks";
  private static final String FROM_LONG = "from";
  private static final String AFTER_LONG = "after";
  private static final String SINGLE_TASK_LONG = "single-task";
  private static final String UNTIL_TASK_LONG = "until";
  private static final String BEFORE_TASK_LONG = "before";
  private static final String RUNNERS_CONFIG_FILE_LONG = "runners-config";
  private static final String LOGS_DIR_LONG = "logs";
  private static final String LOG_ONLY_TASK_LONG = "log-only-task";
  private static final String LOG_EXCLUDE_TASK_LONG = "no-log-task";
  private static final String SHOW_STD_OUTS_LONG = "show-std-outs";
  private static final String QUIET_LONG = "quiet";
  private static final String ABORT_IF_WARNINGS_LONG = "abort-if-warinings";

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

  private static final String LOGS_DIR_DESCRIPTION =
    "Directory to save tasks' output (stdout and stderr, in separated files). By default, no output is saved. If this option is provided, all task's output will be logged by default. You can select which tasks to log with --"
      + LOG_ONLY_TASK_LONG + " or --" + LOG_EXCLUDE_TASK_LONG;
  private static final String LOG_ONLY_TASK_DESCRIPTION =
    "Log task(s). Task id(s) whose output will be logged, other tasks' output will be ignored. This parameter is incompatible with --"
      + LOG_EXCLUDE_TASK_LONG + ". If you use this option, you must provide a log directory with --" + LOGS_DIR_LONG;
  private static final String LOG_EXCLUDE_TASK_DESCRIPTION =
    "Do not log task(s). Task id(s) whose output will be ignored, other tasks' output will be saved. This parameter is incompatible with --"
      + LOG_ONLY_TASK_LONG + ". If you use this option, you must provide a log directory with --" + LOGS_DIR_LONG;

  private static final String SHOW_STD_OUTS_DESCRIPTION = "Forward task stdout/stderr to the compi stdout/stderr";
  private static final String QUIET_DESCRIPTION = "Do not output compi logs to the console";
  private static final String ABORT_IF_WARNINGS_DESCRIPTION = "Abort pipeline run if there are warnings on pipeline validation";

  private static final String DEFAULT_NUM_PARALLEL_TASKS = "6";

  private String[] commandLineArgs;

  public RunCommand(String[] commandLineArgs) {
    this.commandLineArgs = commandLineArgs;
  }

  @Override
  public void execute(final Parameters parameters) throws IOException {

    String pipelineFile = parameters.getSingleValueString(super.getOption(PIPELINE_FILE));
    Integer compiThreads = parameters.getSingleValue(super.getOption(NUM_PARALLEL_TASKS));
    boolean hasFrom = parameters.hasOption(super.getOption(FROM));
    boolean hasAfter = parameters.hasOption(super.getOption(AFTER));
    boolean hasSingleTask = parameters.hasOption(super.getOption(SINGLE_TASK));
    boolean hasUntilTask = parameters.hasOption(super.getOption(UNTIL_TASK));
    boolean hasBeforeTask = parameters.hasOption(super.getOption(BEFORE_TASK));
    boolean hasLogDir = parameters.hasOption(super.getOption(LOGS_DIR));
    boolean hasLogOnlyTasks = parameters.hasOption(super.getOption(LOG_ONLY_TASK));
    boolean hasExcludeLogTasks = parameters.hasOption(super.getOption(LOG_EXCLUDE_TASK));
    boolean hasShowStdOuts = parameters.hasOption(super.getOption(SHOW_STD_OUTS));
    boolean hasQuiet = parameters.hasOption(super.getOption(QUIET));
    boolean hasAbortIfWarnings = parameters.hasOption(super.getOption(ABORT_IF_WARNINGS));

    if (hasQuiet) {
      silenceConsoleLog();
    }

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
    if (hasLogOnlyTasks && hasExcludeLogTasks) {
      throw new IllegalArgumentException(
        "--" + LOG_ONLY_TASK_LONG + " and --" + LOG_EXCLUDE_TASK_LONG + " are incompatible"
      );
    }
    if ((hasLogOnlyTasks || hasExcludeLogTasks) && !hasLogDir) {
      throw new IllegalArgumentException(
        "--" + LOGS_DIR_LONG + " is mandatory if --" + (hasLogOnlyTasks ? LOG_ONLY_TASK_LONG : LOG_EXCLUDE_TASK_LONG)
          + " is used"
      );
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

    File runnersFile = null;
    if (parameters.hasOption(super.getOption(RUNNERS_CONFIG_FILE))) {
      runnersFile = new File(parameters.getSingleValueString(super.getOption(RUNNERS_CONFIG_FILE)));
      if (!runnersFile.exists()) {
        throw new IllegalArgumentException("The runners file does not exist: " + runnersFile);
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

    File logsDir = null;
    if (parameters.hasOption(super.getOption(LOGS_DIR))) {
      logsDir = new File(parameters.getSingleValueString(super.getOption(LOGS_DIR)));
      if (!logsDir.exists()) {
        throw new IllegalArgumentException("Log dir (" + logsDir + ") does not exist or is not accessible");
      }
      if (!logsDir.isDirectory()) {
        throw new IllegalArgumentException("Log dir (" + logsDir + ") is not a directory");
      }
    }
    List<String> logOnlyTasks =
      hasLogOnlyTasks ? parameters.getAllValuesString(super.getOption(LOG_ONLY_TASK)) : null;
    List<String> logExcludeTasks =
      hasExcludeLogTasks ? parameters.getAllValuesString(super.getOption(LOG_EXCLUDE_TASK)) : null;

    LOGGER.info("Compi running with: ");
    LOGGER.info("Pipeline file - " + pipelineFile);
    LOGGER.info("Max number of parallel tasks - " + compiThreads);

    if (parameters.hasOption(super.getOption(PARAMS_FILE))) {
      LOGGER.info("Params file - " + parameters.getSingleValue(super.getOption(PARAMS_FILE)));
    }

    if (runnersFile != null) {
      LOGGER.info("Runners file - " + runnersFile);
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
    if (logsDir != null) {
      LOGGER.info("Logging task's output to dir - " + logsDir);
    }

    try {

      CLIApplication pipelineApplication =
        newPipelineCLIApplication(
          pipelineFile,
          buildConfiguration(
            pipelineFile,
            runnersFile,
            compiThreads,
            fromTasks,
            afterTasks,
            singleTask,
            untilTask,
            beforeTask,
            logsDir,
            logOnlyTasks,
            logExcludeTasks,
            hasShowStdOuts,
            hasAbortIfWarnings
          ), this.commandLineArgs
        );

      pipelineApplication.run(getPipelineParameters(this.commandLineArgs));

    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      LOGGER.severe(e.getClass() + ": " + e.getMessage());
    } catch (PipelineValidationException e) {
      LOGGER.severe("Pipeline is not valid");
      logValidationErrors(e.getErrors());
    }
  }

  private void silenceConsoleLog() {
    Logger root = Logger.getLogger("");
    for (Handler handler : root.getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        handler.setLevel(Level.OFF);
      }
    }
  }

  private CompiRunConfiguration buildConfiguration(
    String pipelineFile, File runnersFile, Integer compiThreads, List<String> fromTasks, List<String> afterTasks,
    String singleTask,
    String untilTask, String beforeTask, File logDir, List<String> logOnlyTasks, List<String> logExcludeTasks,
    boolean showStdOuts, boolean abortIfWarnings
  ) throws IllegalArgumentException, IOException, PipelineValidationException {

    List<ValidationError> errors = new ArrayList<>();

    Pipeline pipeline = fromFile(new File(pipelineFile), errors);

    logValidationErrors(errors);

    if (errors.stream().filter(error -> !error.getType().isError()).count() > 0 && abortIfWarnings) {
      throw new IllegalArgumentException("Pipeline has warnings and --"+ABORT_IF_WARNINGS_LONG+" option was used. Aborting");
    }
    
    final CompiRunConfiguration.Builder builder = forPipeline(pipeline);
    builder.whichRunsAMaximumOf(compiThreads);

    if (runnersFile != null) {
      builder.whichRunsTasksUsingCustomRunners(runnersFile);
    }

    if (singleTask != null) {
      builder.whichRunsTheSingleTask(singleTask);
    }
    if (fromTasks != null) {
      builder.whichStartsFromTasks(fromTasks);
    }
    if (afterTasks != null) {
      builder.whichRunsTasksAfterTasks(afterTasks);
    }
    if (untilTask != null) {
      builder.whichRunsUntilTask(untilTask);
    }
    if (beforeTask != null) {
      builder.whichRunsTasksBeforeTask(beforeTask);
    }
    if (logDir != null) {
      builder.whichLogsOutputsToDir(logDir);
      if (logOnlyTasks != null) {
        builder.whichOnlyLogsTasks(logOnlyTasks);
      } else if (logExcludeTasks != null) {
        builder.whichDoesNotLogTasks(logExcludeTasks);
      }
    }
    if (showStdOuts) {
      builder.whichShowsStdOuts();
    }
    
    CompiRunConfiguration configuration = builder.build();
    return configuration;
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
    options.add(getLogsDir());
    options.add(getLogOnlyTasks());
    options.add(getLogExcludeTasks());
    options.add(getRunSingleTask());
    options.add(getRunFromTasks());
    options.add(getRunAfterTasks());
    options.add(getRunUntilTask());
    options.add(getRunBeforeTask());
    options.add(getRunnersConfigFile());
    options.add(getShowStdOuts());
    options.add(getQuiet());
    options.add(getAbortIfWarnings());

    return options;
  }

  private Option<?> getAbortIfWarnings() {
    return new FlagOption(
      ABORT_IF_WARNINGS_LONG, ABORT_IF_WARNINGS,
      ABORT_IF_WARNINGS_DESCRIPTION
    );
  }

  private Option<?> getShowStdOuts() {
    return new FlagOption(
      SHOW_STD_OUTS_LONG, SHOW_STD_OUTS,
      SHOW_STD_OUTS_DESCRIPTION
    );
  }

  private Option<?> getQuiet() {
    return new FlagOption(
      QUIET_LONG, QUIET,
      QUIET_DESCRIPTION
    );
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

  private Option<?> getLogsDir() {
    return new StringOption(
      LOGS_DIR_LONG, LOGS_DIR,
      LOGS_DIR_DESCRIPTION, true, true, false
    );
  }

  private Option<?> getLogOnlyTasks() {
    return new StringOption(
      LOG_ONLY_TASK_LONG, LOG_ONLY_TASK,
      LOG_ONLY_TASK_DESCRIPTION, true, true, true
    );
  }

  private Option<?> getLogExcludeTasks() {
    return new StringOption(
      LOG_EXCLUDE_TASK_LONG, LOG_EXCLUDE_TASK,
      LOG_EXCLUDE_TASK_DESCRIPTION, true, true, true
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
