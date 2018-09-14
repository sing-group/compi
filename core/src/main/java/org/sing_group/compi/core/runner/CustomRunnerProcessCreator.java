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
