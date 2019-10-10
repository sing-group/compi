/*-
 * #%L
 * Compi Core
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
package org.sing_group.compi.core.validation;

import static java.util.Arrays.asList;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_PARAMETER;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_TASK_ID;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_UNSUPPORTED;
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
import org.sing_group.compi.xmlio.XmlSchemaValidation;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.xml.sax.SAXException;

public class PipelineValidator {

  public static enum ValidationErrorType {
    XML_SCHEMA_UNSUPPORTED(true),
    XML_SCHEMA_VALIDATION_ERROR(true),
    NON_DECLARED_PARAMETER(true),
    NON_DECLARED_TASK_ID(true),
    PARAMETER_NAME_FOUND_IN_CODE(false);

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
    this.clearValidationStatus();

    try {
      String schemaVersion = XmlSchemaValidation.getSchemaVersion(this.pipelineFile.toString());
      String xsdFile = "xsd/pipeline-" + schemaVersion + ".xsd";

      if (!XmlSchemaValidation.existsSchema(xsdFile)) {
        String schemaName = XmlSchemaValidation.getSchemaName(this.pipelineFile.toString());
        return asList(
          new ValidationError(XML_SCHEMA_UNSUPPORTED, "Unsupported schema. Schema file not found for " + schemaName)
        );
      }

      XmlSchemaValidation.validateXmlSchema(this.pipelineFile.toString(), xsdFile);

      this.pipeline = PipelineParserFactory.createPipelineParser().parsePipeline(this.pipelineFile);

      this.checkTaskParametersAreDeclared();
      this.checkAfterIncludesOnlyExistentTasks();
      this.checkParameterNamesFoundInCode();

      this.wasValidated = true;
    } catch (IllegalArgumentException | SAXException e) {
      String errorLocation = extractErrorLocationMessage(e.toString());
      errors.add(
        new ValidationError(
          XML_SCHEMA_VALIDATION_ERROR,
          e.getMessage() + (errorLocation.isEmpty() ? "" : " " + errorLocation + ".")
        )
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return errors;
  }

  private static String extractErrorLocationMessage(String string) {
    StringBuilder sb = new StringBuilder();
    int lineNumberIndex = string.indexOf("lineNumber");
    if (lineNumberIndex != -1) {
      if (string.indexOf(":", lineNumberIndex) != -1 && string.indexOf(";", lineNumberIndex) != -1) {
        sb.append("Line: ");
        sb.append(string.substring(string.indexOf(":", lineNumberIndex) + 2, string.indexOf(";", lineNumberIndex)));
      }
      int columnNumberIndex = string.indexOf("columnNumber:");
      if (columnNumberIndex != -1) {
        if (string.indexOf(":", columnNumberIndex) != -1 && string.indexOf(";", columnNumberIndex) != -1) {
          sb.append(", Column: ");
          sb.append(
            string.substring(string.indexOf(":", columnNumberIndex) + 2, string.indexOf(";", columnNumberIndex))
          );
        }
      }
    }
    return sb.toString();
  }

  private void checkParameterNamesFoundInCode() {
    for (Task t : this.pipeline.getTasks()) {
      for (ParameterDescription pd : this.pipeline.getParameterDescriptions()) {
        if (pd.isGlobal())
          continue;
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

      for (String afterTaskId : t.getAfterList()) {
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
