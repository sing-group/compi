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

import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * Obtains the values of the task foreach tag when the element attribute
 * contains "var"
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ListLoopValuesGenerator extends AbstractLoopValuesGenerator {

  private final List<String> toExecute;

  public ListLoopValuesGenerator(VariableResolver resolver, Foreach foreach) {
    super(resolver, foreach);
    this.toExecute = new LinkedList<>();
  }

  /**
   * Splits all the values in the task source tag
   * 
   * @param source
   *          Indicates the content of the task source tag
   */
  @Override
  protected List<String> getValuesFromResolvedSource(String source) {

    final String[] sourceStrings = source.length() == 0 ? new String[0] : source.split(",");
    for (final String s : sourceStrings) {
      this.toExecute.add(s);
    }
    return this.toExecute;
  }
}
