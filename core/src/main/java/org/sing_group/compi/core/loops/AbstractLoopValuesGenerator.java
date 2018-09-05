package org.sing_group.compi.core.loops;

import java.util.List;

import org.sing_group.compi.core.TextVariableResolver;
import org.sing_group.compi.core.VariableResolver;

public abstract class AbstractLoopValuesGenerator implements LoopValuesGenerator {

  protected VariableResolver resolver;
  
  public AbstractLoopValuesGenerator(VariableResolver resolver) {
    this.resolver = resolver;
  }
  
  @Override
  public List<String> getValues(String source) {
    source = resolveCommandParameters(source);
    return this.getValuesFromResolvedSource(source);
  }
  
  protected abstract List<String> getValuesFromResolvedSource(String source);
  
  private String resolveCommandParameters(String source) {
    TextVariableResolver textVariableResolver = new TextVariableResolver(this.resolver);
    String resolvedString = textVariableResolver.resolveAllVariables(source);
    return resolvedString;
  }
}
