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

import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;
import org.xml.sax.SAXException;

public class PipelineValidator {

  public static enum ValidationErrorType {
    XML_SCHEMA_VALIDATION_ERROR(true), NON_DECLARED_PARAMETER(true),
    NON_DECLARED_TASK_ID(true);

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

      this.wasValidated = true;
    } catch (IllegalArgumentException | SAXException e) {
      errors.add(new ValidationError(XML_SCHEMA_VALIDATION_ERROR, e.getMessage()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return errors;
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

      for (String afterTaskId : t.getAfter().split("\\s*,\\s*")) {
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
