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
