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
package org.sing_group.compi.core.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapVariableResolver extends DecoratedVariableResolver {
  private static final long serialVersionUID = 1L;

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
