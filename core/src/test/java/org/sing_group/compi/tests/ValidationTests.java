package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

public class ValidationTests {

  private int numberOfErrorsOfType(PipelineValidator.ValidationErrorType type, List<ValidationError> errors) {
    return errors.stream().map(error -> (error.getType().equals(type) ? 1 : 0)).reduce((x, y) -> x + y).orElse(0);
  }

  @Test
  public void testIncorrectXMLSyntaxRaisesError() {
    String pipelineName = "pipelineParsingException.xml";
    
    List<ValidationError> errors = validatePipeline(pipelineName);

    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors));
  }
  
  @Test
  public void testRepeatedParamDescriptionNameRaisesErrors() {
    String pipelineName = "pipelineRepeatedParamDescriptionName.xml";
    
    List<ValidationError> errors = validatePipeline(pipelineName);

    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors)); // ${pipeline}
  }
  
  private List<ValidationError> validatePipeline(String pipelineName) {
    PipelineValidator validator =
      new PipelineValidator(
        new File(ClassLoader.getSystemResource(pipelineName).getFile())
        );
    
    List<ValidationError> errors = validator.validate();
    return errors;
  }
}
