package org.sing_group.compi.core.resolver;

import static java.util.Arrays.asList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Scanner;

import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;

public class VariableResolverUtils {

  private VariableResolver resolver;

  public VariableResolverUtils(VariableResolver resolver) {
    this.resolver = resolver;
  }

  public void addVariablesToEnvironmentForTask(Task task, ProcessBuilder builder) {

    task.getParameters().forEach(parameter -> {
      builder.environment().put(parameter, this.resolver.resolveVariable(parameter));
    });
    
    if (task instanceof Foreach && ((Foreach) task).getForeachIteration() != null) {
      Foreach forEachTask = (Foreach) task;
      builder.environment().put(
        forEachTask.getForeachIteration().getTask().getAs(), forEachTask.getForeachIteration().getIterationValue()
      );
    }
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
      final String expandVarsProgram =
        "envsubstpath=$(which envsubst); $envsubstpath < " + toResolveTextFile + " > " + resolvedTextFile;
      ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList("/bin/sh", "-c", expandVarsProgram));

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

    /*
     * final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); String
     * resolvedString = text; final Matcher matcher = pattern.matcher(text);
     * while (matcher.find()) { String varName = matcher.group(1); String
     * resolvedVarName = this.resolver.resolveVariable(varName); if
     * (resolvedVarName != null) { resolvedString =
     * resolvedString.replaceFirst("\\$\\{"+varName+"\\}", resolvedVarName); } }
     * return resolvedString;
     */
  }

  private String getFileContents(File resolvedTextFile) throws FileNotFoundException {
    try (Scanner resultsScanner = new Scanner(resolvedTextFile)) {
      return resultsScanner.useDelimiter("\\Z").next();
    }
  }
}
