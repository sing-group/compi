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
package org.sing_group.compi.core;

import java.util.List;
import java.util.stream.Collectors;

import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

/**
 * A class for representing pipeline validation exceptions
 * 
 * This class gives access to the list of errors and/or warnings produced during
 * validation.
 * 
 * @see PipelineValidator
 * @see ValidationError
 */
@SuppressWarnings("serial")
public class PipelineValidationException extends Exception {

  private List<ValidationError> errors;

  public PipelineValidationException(List<ValidationError> errors) {
    super(errors.stream().map(e -> e.getMessage()).collect(Collectors.joining(", ")));
    this.errors = errors;
    
  }

  public List<ValidationError> getErrors() {
    return errors;
  }
}
