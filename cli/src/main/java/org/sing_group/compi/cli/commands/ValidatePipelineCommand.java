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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.cli.CompiException;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
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
      String errorMessage = "Pipeline file not found: " + pipelineNameFile;
      throw new CompiException(
        "PipelineNotFound",
        errorMessage, new IllegalArgumentException(errorMessage)
      );
    }

    List<ValidationError> errors = new PipelineValidator(pipelineFile).validate();
    logValidationErrors(errors, LOGGER);

    if (
      errors.stream().filter(error -> error.getType().isError())
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
    options.add(getPipelineFileOption());

    return options;
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE,
      PIPELINE_FILE_DESCRIPTION, CommonParameters.PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  public static void logValidationErrors(List<ValidationError> errors, Logger logger) {
    errors.stream().forEach(error -> {
      if (error.getType().isError()) {
        logger.severe(error.toString());
      } else {
        logger.warning(error.toString());
      }
    });
  }
}
