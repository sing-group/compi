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
package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_PARAMETER;
import static org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType.NON_DECLARED_TASK_ID;
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
