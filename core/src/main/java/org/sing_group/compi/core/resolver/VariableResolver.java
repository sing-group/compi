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

import java.io.Serializable;
import java.util.Set;

/**
 * A variable resolver obtains the value of a variable given its name.
 *
 */
public interface VariableResolver extends Serializable {

  /**
   * Get the content of the variable.
   *
   * @param variable
   *          the variable to resolve
   * @return the content of the variable
   * @throws IllegalArgumentException
   *           if the variable does not exist
   */
  String resolveVariable(String variable)
    throws IllegalArgumentException;

  /**
   * Get all resolvable variables that this resolver can resolve.
   * 
   * @return The set of all resolvable variables
   */
  Set<String> getVariableNames();
}
