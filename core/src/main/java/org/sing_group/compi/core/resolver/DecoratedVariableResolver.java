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
