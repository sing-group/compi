package org.sing_group.compi.core.resolver;

import java.util.HashSet;
import java.util.Set;

public class DecoratedVariableResolver implements VariableResolver {

  private VariableResolver innerResolver;

  public DecoratedVariableResolver() {
  }
  
  public DecoratedVariableResolver(VariableResolver innerResolver) {
    this.innerResolver = innerResolver;
  }
  
  @Override
  public String resolveVariable(String variable) throws IllegalArgumentException {
    if (this.innerResolver == null) {
      return null;
    }
    return this.innerResolver.resolveVariable(variable);
  }
  
  @Override
  public Set<String> getVariableNames() {
    if (this.innerResolver == null) {
      return new HashSet<>();
    }
    return this.innerResolver.getVariableNames();
  }
}
