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

import static java.util.Arrays.asList;
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

      ProcessBuilder processBuilder =
        new ProcessBuilder(
          createShellCommand(
            "envsubstpath=$(which envsubst); $envsubstpath < " + toResolveTextFile + " > " + resolvedTextFile
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
      return resultsScanner.useDelimiter("\\Z").next();
    }
  }
}
