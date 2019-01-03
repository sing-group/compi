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
package org.sing_group.compi.core.runner;

import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.resolver.VariableResolver;

public class DefaultProcessCreator implements ProcessCreator {

  private VariableResolver resolver;

  public DefaultProcessCreator(VariableResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public Process createProcess(Task task) {
    return ProcessCreator.createProcess(
      task.getInterpreter() == null ? task.getToExecute() : task.getInterpreter(), task, resolver
    );
  }
}
