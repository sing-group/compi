package org.sing_group.compi.core.runner;

import java.io.IOException;

import static org.sing_group.compi.core.runner.ProcessCreator.createShellCommand;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.resolver.VariableResolverUtils;
import org.sing_group.compi.xmlio.entities.Task;

public class DefaultProcessCreator implements ProcessCreator {

  private VariableResolver resolver;

  public DefaultProcessCreator(VariableResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public Process createProcess(Task task) {
    try {
      ProcessBuilder builder =
        new ProcessBuilder(
          createShellCommand(task.getInterpreter() == null ? task.getToExecute() : task.getInterpreter())
        );

      VariableResolverUtils resolverUtils = new VariableResolverUtils(resolver);
      resolverUtils.addVariablesToEnvironmentForTask(task, builder);

      return builder.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
