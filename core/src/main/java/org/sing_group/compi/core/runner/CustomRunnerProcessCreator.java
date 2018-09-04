package org.sing_group.compi.core.runner;

import java.io.IOException;

import org.sing_group.compi.core.TextVariableResolver;
import org.sing_group.compi.core.VariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;
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
    this.runner.getRunnerCode();
    
    //resolve code
    String runnerResolvedCode = this.runner.getRunnerCode();
    runnerResolvedCode = runnerResolvedCode.replace("${task-code}", task.getToExecute());
    runnerResolvedCode = runnerResolvedCode.replace("${task-id}", task.getId());
    final TextVariableResolver textResolver = new TextVariableResolver(
      
      (var) -> {
        
        if (task instanceof Foreach && var.equals(((Foreach) task).getForeachIteration().getTask().getAs())) {
          return ((Foreach) task).getForeachIteration().getIterationValue();
        }
        return resolver.resolveVariable(var);
      }
     );
    
    runnerResolvedCode = textResolver.resolveAllVariables(runnerResolvedCode);
    String[] commandsToExecute = { "/bin/sh", "-c", runnerResolvedCode };
    try {
      return Runtime.getRuntime().exec(commandsToExecute);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
