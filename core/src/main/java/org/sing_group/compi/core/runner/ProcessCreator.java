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
package org.sing_group.compi.core.runner;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.resolver.VariableResolverUtils;

public interface ProcessCreator {
  public Process createProcess(Task t);

  public static List<String> createShellCommand(String program) {
    final List<String> SHELL_COMMAND = asList("/bin/bash", "-c");
    List<String> commandsToExecute = new ArrayList<>(SHELL_COMMAND);
    commandsToExecute.add(program);
    return commandsToExecute;
  }

  public static Process createProcess(String source, Task task, VariableResolver resolver) {
    try {
      ProcessBuilder builder =
        new ProcessBuilder(
          createShellCommand(source)
        );

      VariableResolverUtils resolverUtils = new VariableResolverUtils(resolver);
      resolverUtils.addVariablesToEnvironmentForTask(task, builder);

      return builder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
