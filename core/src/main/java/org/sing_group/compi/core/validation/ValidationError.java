package org.sing_group.compi.core.validation;

import org.sing_group.compi.core.validation.PipelineValidator.ValidationErrorType;

public class ValidationError {



  private String message;
  private ValidationErrorType type;
  
  public ValidationError(ValidationErrorType type, String message) {
    this.type = type;
    this.message = message;
  }
  
  public String getMessage() {
    return message;
  }
  
  public ValidationErrorType getType() {
    return type;
  }
  
  @Override
  public String toString() {
    return type.name()+": "+this.message;
  }
}
