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
