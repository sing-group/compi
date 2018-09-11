package org.sing_group.compi.core.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapVariableResolver extends DecoratedVariableResolver {

  private Map<String, String> variables = new HashMap<>();

  public MapVariableResolver() {}

  public MapVariableResolver(VariableResolver inner, Map<String, String> variables) {
    super(inner);
    this.variables = variables;
  }
  
  public MapVariableResolver(VariableResolver inner) {
    super(inner);
  }

  public MapVariableResolver(Map<String, String> variables) {
    this.variables = variables;
  }

  @Override
  public String resolveVariable(String variable) throws IllegalArgumentException {
    String result = this.variables.get(variable);
    return result == null ? super.resolveVariable(variable) : result;
  }

  @Override
  public Set<String> getVariableNames() {
    Set<String> variableNames = new HashSet<>();
    variableNames.addAll(this.variables.keySet());
    variableNames.addAll(super.getVariableNames());
    return variableNames;
  }
  
  public void addVariable(String name, String value) {
    this.variables.put(name, value);
  }
}
