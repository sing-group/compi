/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2020 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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

import org.sing_group.compi.core.Dependency;
import org.sing_group.compi.core.pipeline.Foreach;

public class ForeachIterationDependency extends Dependency<Foreach> {

  public ForeachIterationDependency(Foreach parentOnTask, Foreach parentDependantTask) {
    super(parentOnTask, parentDependantTask);
  }
}
