package org.sing_group.compi.core.runner;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.sing_group.compi.xmlio.entities.Task;

public interface ProcessCreator {
  public Process createProcess(Task t);
  
  public static List<String> createShellCommand(String program) {
    final List<String> SHELL_COMMAND = asList("/bin/bash", "-c");
    List<String> commandsToExecute = new ArrayList<>(SHELL_COMMAND);
    commandsToExecute.add(program);
    return commandsToExecute;
  }
}
