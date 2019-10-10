/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
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
 * #L%
 */
package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;
import static org.sing_group.compi.xmlio.PipelineParserFactory.createPipelineParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.sing_group.compi.cli.PipelineCLIApplication;
import org.sing_group.compi.core.CompiRunConfiguration;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class HelpTaskCommand extends AbstractCommand {
  private static final Logger LOGGER = getLogger(HelpTaskCommand.class.getName());

  private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
  private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
  private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;

  private static final String TASK_NAME_ = "t";
  private static final String TASK_NAME_LONG = "task";
  private static final String TASK_NAME_DESCRIPTION = "Task identifier";

  private String[] commandLineArgs;

  public HelpTaskCommand(String[] commandLineArgs) {
    this.commandLineArgs = commandLineArgs;
  }

  @Override
  public void execute(final Parameters parameters) throws Exception {
    String pipelineFileName = parameters.getSingleValue(super.getOption(PIPELINE_FILE));
    String taskId = parameters.getSingleValue(super.getOption(TASK_NAME_));

    File pipelineFile = new File(pipelineFileName);

    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException(
        "Pipeline file not found: " + pipelineFileName
      );
    }

    Pipeline pipelineObject = createPipelineParser().parsePipeline(pipelineFile);

    Optional<Task> task = pipelineObject.getTasks().stream()
      .filter(t -> t.getId().equals(taskId))
      .findFirst();

    if (!task.isPresent()) {
      LOGGER.warning("The specified task id (" + taskId + ") is not defined in the pipeline");
    } else {
      showTaskHelp(pipelineFileName, pipelineObject, task.get());
    }
  }

  private void showTaskHelp(String pipelineFileName, Pipeline pipelineObject, Task task) {
    if (!task.getMetadata().getDescription().isEmpty()) {
      System.err.println(task.getId() + ": " + task.getMetadata().getDescription());
    }
    System.err.println("Task parameters:");

    final CompiRunConfiguration.Builder builder = CompiRunConfiguration.forPipeline(pipelineObject);
    CompiRunConfiguration configuration = builder.whichRunsTheSingleTask(task.getId()).build();
    CLIApplication pipelineApplication =
      PipelineCLIApplication.newPipelineCLIApplication(
        pipelineFileName,
        configuration,
        this.commandLineArgs
      );
    pipelineApplication.run(new String[] {
      "help-task"
    });
  }

  @Override
  public String getDescription() {
    return "Shows the help of a task.";
  }

  @Override
  public String getName() {
    return "help-task";
  }

  @Override
  public String getDescriptiveName() {
    return "Show help task";
  }

  @Override
  protected List<Option<?>> createOptions() {
    final List<Option<?>> options = new ArrayList<>();
    options.add(getPipelineFileOption());
    options.add(getTaskName());

    return options;
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE,
      PIPELINE_FILE_DESCRIPTION, CommonParameters.PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  private Option<?> getTaskName() {
    return new StringOption(
      TASK_NAME_LONG, TASK_NAME_,
      TASK_NAME_DESCRIPTION, false, true
    );
  }
}
