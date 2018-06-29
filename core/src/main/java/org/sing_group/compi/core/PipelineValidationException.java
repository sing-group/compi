package org.sing_group.compi.core;

import java.util.List;

import org.sing_group.compi.core.validation.ValidationError;

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
