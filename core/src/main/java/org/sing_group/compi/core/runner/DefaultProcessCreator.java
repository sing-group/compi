package org.sing_group.compi.core.runner;

import java.io.IOException;

import org.sing_group.compi.xmlio.entities.Task;

public class DefaultProcessCreator implements ProcessCreator {

  @Override
  public Process createProcess(Task task) {
    String[] commandsToExecute = { "/bin/sh", "-c", task.getToExecute() };
    try {
      return Runtime.getRuntime().exec(commandsToExecute);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
