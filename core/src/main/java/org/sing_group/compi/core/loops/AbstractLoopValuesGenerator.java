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
package org.sing_group.compi.core.loops;

import java.util.List;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.resolver.VariableResolverUtils;

public abstract class AbstractLoopValuesGenerator implements LoopValuesGenerator {

  protected VariableResolver resolver;
  private Foreach foreach;
  
  public AbstractLoopValuesGenerator(VariableResolver resolver, Foreach foreach) {
    this.resolver = resolver;
    this.foreach = foreach;
  }
  
  @Override
  public List<String> getValues(String source) {
    source = resolveCommandParameters(source);
    return this.getValuesFromResolvedSource(source);
  }
  
  protected abstract List<String> getValuesFromResolvedSource(String source);
  
  private String resolveCommandParameters(String source) {
    VariableResolverUtils resolverUtils = new VariableResolverUtils(this.resolver);
    String resolvedString = resolverUtils.resolveAllVariables(source, foreach);
    return resolvedString;
  }
}
