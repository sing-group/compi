package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_SHORT_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.SEMANTIC_ERROR_RESERVED_PARAMETER_NAME;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.WARNING_MISSING_PARAM_DESCRIPTION;

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
  public void testParametersWithoutDescriptionRaisesWarning() {
    String pipelineName = "pipelineNoParameterDescriptions.xml";
    
    List<ValidationError> errors = validatePipeline(pipelineName);

    assertEquals(2, numberOfErrorsOfType(WARNING_MISSING_PARAM_DESCRIPTION, errors));
  }
  
  @Test
  public void testIllegalParameterNamesRaisesErrors() {
    String pipelineName = "pipelineIllegalParameterNames.xml";
    
    List<ValidationError> errors = validatePipeline(pipelineName);

    assertEquals(1, numberOfErrorsOfType(SEMANTIC_ERROR_RESERVED_PARAMETER_NAME, errors)); // ${pipeline}
    assertEquals(1, numberOfErrorsOfType(SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_NAME, errors)); // <param name="num-threads">
    assertEquals(1, numberOfErrorsOfType(SEMANTIC_ERROR_RESERVED_PARAMETER_DESCRIPTION_SHORT_NAME, errors)); // <param shortName="pa">
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
