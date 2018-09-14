package org.sing_group.compi.core.runner;

import static java.util.Arrays.asList;

import java.io.IOException;

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
    String[] commandsToExecute = { "/bin/sh", "-c", task.getInterpreter()==null?task.getToExecute():task.getInterpreter() };
    
    try {
      
      ProcessBuilder builder = new ProcessBuilder(asList(commandsToExecute));
      
      VariableResolverUtils resolverUtils = new VariableResolverUtils(resolver);
      resolverUtils.addVariablesToEnvironmentForTask(task, builder);
      
      return builder.start();
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
