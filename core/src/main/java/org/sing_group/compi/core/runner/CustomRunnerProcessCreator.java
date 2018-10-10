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

import java.io.IOException;

import static org.sing_group.compi.core.runner.ProcessCreator.createShellCommand;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.resolver.VariableResolverUtils;
import org.sing_group.compi.xmlio.entities.Task;
import org.sing_group.compi.xmlio.entities.runners.Runner;


public class CustomRunnerProcessCreator implements ProcessCreator {

  private Runner runner;
  private VariableResolver resolver;

  public CustomRunnerProcessCreator(Runner runner, VariableResolver resolver) {
    this.runner = runner;
    this.resolver = resolver;
  }

  @Override
  public Process createProcess(Task task) {
    try {
      ProcessBuilder builder = new ProcessBuilder(createShellCommand(this.runner.getRunnerCode()));

      VariableResolverUtils resolverUtils = new VariableResolverUtils(this.resolver);
      resolverUtils.addVariablesToEnvironmentForTask(task, builder);

      return builder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
