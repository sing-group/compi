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
package org.sing_group.compi.core;

import java.util.List;

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
    this.errors = errors;
  }

  public List<ValidationError> getErrors() {
    return errors;
  }
}
