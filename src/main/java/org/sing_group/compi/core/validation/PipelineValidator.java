package org.sing_group.compi.core.validation;

import static java.util.stream.Collectors.toSet;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_SHORT_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_SHORT_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.WARNING_MISSING_PARAM_DESCRIPTION;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.yacli.command.option.Option;

public class PipelineValidator {

  public static enum ValidationErrorType {
    WARNING_MISSING_PARAM_DESCRIPTION(false), XML_SCHEMA_VALIDATION_ERROR(true),
    SEMANTIC_ERROR_RESERVED_PARAMETER_NAME(true), SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_NAME(true),
    SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_SHORT_NAME(true), SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_NAME(true),
    SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_SHORT_NAME(true);

    private boolean isError;
    
    private ValidationErrorType(boolean isError) {
      this.isError = isError;
    }
    
    public boolean isError() {
      return isError;
    }
  }

  private File pipelineFile;

  private Pipeline pipeline;
  private List<ValidationError> errors = new ArrayList<>();

  public PipelineValidator(File pipelineFile) {
    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException("Pipeline file not found: " + pipelineFile);
    }
    this.pipelineFile = pipelineFile;
  }

  
  public List<ValidationError> validate() {
    clearValidationStatus();

    try {
      final File xsdFile = new File(getClass().getClassLoader().getResource("xsd/pipeline.xsd").getFile());
      DOMparsing.validateXMLSchema(this.pipelineFile.toString(), xsdFile);
      
      this.pipeline = PipelineParserFactory.createPipelineParser().parsePipeline(this.pipelineFile);
      checkThatAllParameterNamesAreLegal();

      checkThatAllParametersHaveDescription();

      checkThatParameterDescriptionNamesAreUnique();

    } catch (IllegalArgumentException | SAXException e) {
      errors.add(new ValidationError(XML_SCHEMA_VALIDATION_ERROR, e.getMessage()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return errors;
  }

  private void checkThatParameterDescriptionNamesAreUnique() {
    Set<String> names = new HashSet<>();
    this.pipeline.getParameterDescriptions().stream().forEach(parameterDescription -> {
      if (names.contains(parameterDescription.getName())) {
        this.errors.add(
          new ValidationError(
            SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_NAME,
            "The <param name=\"" + parameterDescription.getName() + "\"> is not unique"
          )
        );
      }
      if (names.contains(parameterDescription.getShortName())) {
        this.errors.add(
          new ValidationError(
            SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_SHORT_NAME,
            "The <param shortName=\"" + parameterDescription.getName() + "\"> is not unique"
          )
        );
      }
      names.add(parameterDescription.getName());
      names.add(parameterDescription.getShortName());
    });

  }

  private void checkThatAllParametersHaveDescription() {
    this.pipeline.getTasksByParameter().keySet().stream().forEach(parameterName -> {
      
      if (this.pipeline.getParameterDescription(parameterName) == null) {
        this.errors.add(
          new ValidationError(
            WARNING_MISSING_PARAM_DESCRIPTION,
            "The parameter \"" + parameterName + "\" has no <param> section for discribing it."
          )
        );
      }
    });
  }

  private void checkThatAllParameterNamesAreLegal() {
    RunCommand command = new RunCommand(new String[] {});
    List<Option<?>> options = command.getOptions();
    Set<String> longNames = options.stream().map(Option::getParamName).collect(toSet());
    Set<String> shortNames = options.stream().map(Option::getShortName).collect(toSet());

    pipeline.getTasksByParameter().keySet().stream().forEach(parameter -> {
      if (longNames.contains(parameter)) {
        errors.add(
          new ValidationError(
            SEMANTIC_ERROR_RESERVED_PARAMETER_NAME,
            "Cannot use a parameter with name \"" + parameter + "\" because it is reserved"
          )
        );
      }
    });

    pipeline.getParameterDescriptions().stream().forEach(parameterDescription -> {
      if (longNames.contains(parameterDescription.getName())) {
        errors.add(
          new ValidationError(
            SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_NAME,
            "Cannot use a <param> with name \"" + parameterDescription.getName() + "\" because it is reserved"
          )
        );
      }
      if (shortNames.contains(parameterDescription.getShortName())) {
        errors.add(
          new ValidationError(
            SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_SHORT_NAME,
            "Cannot use a <param> with short name \"" + parameterDescription.getShortName() + "\" because it is reserved"
          )
        );
      }
    });
  }

  private void clearValidationStatus() {
    errors.clear();
    this.pipeline = null;
  }
}
