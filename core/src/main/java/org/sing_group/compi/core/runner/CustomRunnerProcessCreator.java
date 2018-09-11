package org.sing_group.compi.core.runner;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
      String[] commandsToExecute = {
        "/bin/sh", "-c", this.runner.getRunnerCode()
      };
      ProcessBuilder builder = new ProcessBuilder(asList(commandsToExecute));

      VariableResolverUtils resolverUtils = new VariableResolverUtils(this.resolver);
      resolverUtils.addVariablesToEnvironmentForTask(task, builder);
      
      Map<String, String> runnerExtraVariables = new HashMap<>();
      runnerExtraVariables.put("task_code", task.getToExecute());
      runnerExtraVariables.put("task_id", task.getId());
      builder.environment().putAll(runnerExtraVariables);
      
      return builder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
