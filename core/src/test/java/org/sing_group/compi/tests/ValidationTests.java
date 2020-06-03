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
package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_PARAMETER;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_TASK_ID;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.XML_SCHEMA_VALIDATION_ERROR;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.PARAMETER_NAME_FOUND_IN_CODE;

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

    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors));
  }

  @Test
  public void testInvalidParameterName() {
    String pipelineName = "pipelineInvalidParameterName.xml";

    List<ValidationError> errors = validatePipeline(pipelineName);
    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors));
  }

  @Test
  public void testNonDeclaredParameter() {
    String pipelineName = "pipelineNonDeclaredParameter.xml";

    List<ValidationError> errors = validatePipeline(pipelineName);
    assertEquals(1, numberOfErrorsOfType(NON_DECLARED_PARAMETER, errors));
  }

  @Test
  public void testNotParameterInSource() {
    String pipelineName = "pipelineNotParameterInSource.xml";

    List<ValidationError> errors = validatePipeline(pipelineName);
    assertEquals(1, numberOfErrorsOfType(PARAMETER_NAME_FOUND_IN_CODE, errors));
  }

  @Test
  public void testNonExistantAfterId() throws Exception {
    String pipelineName = "pipelineNonExistantAfterID.xml";
    List<ValidationError> errors = validatePipeline(pipelineName);

    assertEquals(1, numberOfErrorsOfType(NON_DECLARED_TASK_ID, errors));
  }

  @Test
  public void testInvalidSrc() throws Exception {
    String pipelineName = "pipelineNonExistantSrc.xml";
    List<ValidationError> errors = validatePipeline(pipelineName);
    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors));
  }

  @Test
  public void testBothSrcAndBody() throws Exception {
    String pipelineName = "pipelineBothSrcAndBody.xml";
    List<ValidationError> errors = validatePipeline(pipelineName);
    assertEquals(1, numberOfErrorsOfType(XML_SCHEMA_VALIDATION_ERROR, errors));
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
