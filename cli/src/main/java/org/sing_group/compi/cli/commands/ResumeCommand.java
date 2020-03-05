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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiApp;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class ResumeCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(ResumeCommand.class.getName());

  private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
  private static final String QUIET = CommonParameters.QUIET;

  private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
  private static final String QUIET_LONG = CommonParameters.QUIET_LONG;

  private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;
  private static final String QUIET_DESCRIPTION = CommonParameters.QUIET_DESCRIPTION;

  @Override
  public void execute(final Parameters parameters) throws IllegalArgumentException, Exception {
    boolean hasQuiet = parameters.hasOption(super.getOption(QUIET));
    if (hasQuiet) {
      silenceConsoleLog();
    }

    File pipelineFile = new File(parameters.getSingleValueString(super.getOption(PIPELINE_FILE)));

    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException(
        "Pipeline file not found: " + pipelineFile
      );
    }

    LOGGER.info("Compi resume. Trying to rebuild compi app for pipeline file: " + pipelineFile);
    CompiApp compiApp = new CompiApp(pipelineFile);
    LOGGER.info("Rebuild OK");
    compiApp.addTaskExecutionHandler(new CLITaskExecutionHandler());
    for (String logLine : compiApp.getConfig().toString().split("\n")) {
      LOGGER.info(logLine);
    }

    compiApp.run();

  }

  private void silenceConsoleLog() {
    Logger root = Logger.getLogger("");
    for (Handler handler : root.getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        handler.setLevel(Level.OFF);
      }
    }
  }

  @Override
  public String getDescription() {
    return "Resumes a pipeline.";
  }

  @Override
  public String getName() {
    return "resume";
  }

  @Override
  public String getDescriptiveName() {
    return "Resume compi pipeline";
  }

  @Override
  protected List<Option<?>> createOptions() {
    final List<Option<?>> options = new ArrayList<>();
    options.add(getPipelineFileOption());
    options.add(getQuietOption());

    return options;
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE,
      PIPELINE_FILE_DESCRIPTION, CommonParameters.PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  private Option<?> getQuietOption() {
    return new FlagOption(
      QUIET_LONG, QUIET,
      QUIET_DESCRIPTION
    );
  }
}
