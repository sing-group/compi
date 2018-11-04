/*-
 * #%L
 * Compi Core
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
package org.sing_group.compi.core.validation;

import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_PARAMETER;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_TASK_ID;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.ParameterDescription;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.xml.sax.SAXException;

public class PipelineValidator {

  public static enum ValidationErrorType {
    XML_SCHEMA_VALIDATION_ERROR(true), NON_DECLARED_PARAMETER(true), NON_DECLARED_TASK_ID(
      true
    ), PARAMETER_NAME_FOUND_IN_CODE(false);

    private boolean isError;

    private ValidationErrorType(boolean isError) {
      this.isError = isError;
    }

    public boolean isError() {
      return isError;
    }
  }

  private boolean wasValidated = false;
  private File pipelineFile;
  private List<ValidationError> errors = new ArrayList<>();
  private Pipeline pipeline;

  public PipelineValidator(File pipelineFile) {
    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException("Pipeline file not found: " + pipelineFile);
    }
    this.pipelineFile = pipelineFile;
  }

  public List<ValidationError> validate() {
    clearValidationStatus();

    try {

      DOMparsing.validateXMLSchema(this.pipelineFile.toString(), "xsd/pipeline.xsd");

      this.pipeline = PipelineParserFactory.createPipelineParser().parsePipeline(this.pipelineFile);

      checkTaskParametersAreDeclared();
      checkAfterIncludesOnlyExistentTasks();
      checkParameterNamesFoundInCode();

      this.wasValidated = true;
    } catch (IllegalArgumentException | SAXException e) {
      errors.add(new ValidationError(XML_SCHEMA_VALIDATION_ERROR, e.getMessage()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return errors;
  }

  private void checkParameterNamesFoundInCode() {
    for (Task t : this.pipeline.getTasks()) {
      for (ParameterDescription pd : this.pipeline.getParameterDescriptions()) {
        if (pd.isGlobal()) continue;
        if (
          t.getToExecute().contains(pd.getName()) &&
            !t.getParameters().contains(pd.getName()) &&
            (!(t instanceof Foreach && ((Foreach) t).getAs().equals(pd.getName())))
        ) {

          errors.add(
            new ValidationError(
              ValidationErrorType.PARAMETER_NAME_FOUND_IN_CODE,
              "Parameter \"" + pd.getName() + "\" found in code of task " + t.getId()
                + ", but it is neither declared in the 'params' attribute of the "
                + "task nor defined as a 'global' parameter. Is this correct?"
            )
          );
        }
      }
    }

  }

  private void checkTaskParametersAreDeclared() {
    for (Task t : this.pipeline.getTasks()) {
      for (String parameter : t.getParameters()) {

        if (this.pipeline.getParameterDescription(parameter) == null) {
          errors.add(
            new ValidationError(
              NON_DECLARED_PARAMETER,
              "Parameter \"" + parameter + "\" in task id: \"" + t.getId() + "\" was not declared"
            )
          );
        }
      }
    }
  }

  private void checkAfterIncludesOnlyExistentTasks() {
    Set<String> taskIds = this.pipeline.getTasks().stream().map(Task::getId).collect(Collectors.toSet());
    for (Task t : this.pipeline.getTasks()) {
      if (t.getAfter() == null)
        continue;

      for (String afterTaskId : t.getAfter().split("[\\s,]+")) {
        if (!taskIds.contains(afterTaskId)) {
          errors.add(
            new ValidationError(
              NON_DECLARED_TASK_ID,
              "Task id \"" + afterTaskId + "\" declared in after of task: \"" + t.getId() + "\" does not exist"
            )
          );
        }
      }
    }
  }

  private void clearValidationStatus() {
    errors.clear();
    this.wasValidated = false;
    this.pipeline = null;
  }

  public Pipeline getPipeline() {
    if (!this.wasValidated) {
      this.validate();
    }
    if (this.pipeline == null) {
      throw new IllegalStateException("Could not parse the pipeline");
    }

    return this.pipeline;
  }
}
