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

import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.core.runner.ProcessCreator.createShellCommand;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.sing_group.compi.core.CompiRunConfiguration;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;

public class VariableResolverUtils {

  private VariableResolver resolver;

  public VariableResolverUtils(VariableResolver resolver) {
    this.resolver = resolver;
  }

  public void addVariablesToEnvironmentForTask(Task task, ProcessBuilder builder) {
    task.getParameters().forEach(parameter -> {
      if (task.getPipeline().getParameterDescription(parameter).isFlag()) {
        if (this.resolver.resolveVariable(parameter) != null) {
          builder.environment().put(parameter, "yes");
        }
      } else {
        builder.environment().put(parameter, this.resolver.resolveVariable(parameter));
      }
    });

    if (task instanceof ForeachIteration && ((ForeachIteration) task).getParentForeachTask() != null) {
      ForeachIteration forEachTask = (ForeachIteration) task;
      builder.environment().put(
        forEachTask.getAs(), forEachTask.getIterationValue()
      );
    }

    Map<String, String> runnerExtraVariables = new HashMap<>();
    runnerExtraVariables.put("task_code", task.getToExecute());
    runnerExtraVariables.put("task_id", task.getId());
    if (task.getInterpreter() != null) {
      runnerExtraVariables.put("task_interpreter", task.getInterpreter());
    }

    List<String> params = new ArrayList<>(task.getParameters());
    if (task instanceof Foreach) {
      params.add(((Foreach) task).getAs());
    }
    runnerExtraVariables.put("task_params", params.stream().collect(joining(" ")));

    builder.environment().putAll(runnerExtraVariables);

    builder.environment().putAll(
      this.resolver.getVariableNames().stream().filter(
        v -> v.startsWith(CompiRunConfiguration.CONFIGURATION_VARIABLES_PREFIX)
      ).collect(Collectors.toMap(identity(), v -> this.resolver.resolveVariable(v)))
    );

  }

  public String resolveAllVariables(String text, Task task) {

    File toResolveTextFile = null;
    File resolvedTextFile = null;
    try {
      toResolveTextFile = File.createTempFile("compi-resolve-variables", ".txt");
      resolvedTextFile = File.createTempFile("compi-resolved-variables", ".txt");
      try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(toResolveTextFile))) {
        out.write(text.getBytes());
        out.close();
      }

      String envsubst = System.getProperty("envsubst.path", "$(which envsubst)");

      ProcessBuilder processBuilder =
        new ProcessBuilder(
          createShellCommand(
            String.format("envsubstpath=%s; $envsubstpath < %s > %s", envsubst, toResolveTextFile, resolvedTextFile)
          )
        );

      this.addVariablesToEnvironmentForTask(task, processBuilder);
      processBuilder.start().waitFor();

      String toret = getFileContents(resolvedTextFile);

      return toret;

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      asList(toResolveTextFile, resolvedTextFile).forEach(file -> {
        if (file != null && file.exists()) {
          file.delete();
        }
      });
    }
  }

  private String getFileContents(File resolvedTextFile) throws FileNotFoundException {
    try (Scanner resultsScanner = new Scanner(resolvedTextFile)) {
      if (resultsScanner.useDelimiter("\\Z").hasNext()) {
        return resultsScanner.useDelimiter("\\Z").next();
      } else {
        return "";
      }
    }
  }
}
