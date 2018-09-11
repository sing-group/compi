package org.sing_group.compi.core.validation;

import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_REPEATED_PARAM_DESCRIPTION_SHORT_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.xml.sax.SAXException;


public class PipelineValidator {

  public static enum ValidationErrorType {
    XML_SCHEMA_VALIDATION_ERROR(true),
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
      
      DOMparsing.validateXMLSchema(this.pipelineFile.toString(), "xsd/pipeline.xsd");
      
      this.pipeline = PipelineParserFactory.createPipelineParser().parsePipeline(this.pipelineFile);

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

  private void clearValidationStatus() {
    errors.clear();
    this.pipeline = null;
  }
}
