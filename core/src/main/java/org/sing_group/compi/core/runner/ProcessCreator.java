package org.sing_group.compi.core.runner;

import org.sing_group.compi.xmlio.entities.Task;

public interface ProcessCreator {
  public Process createProcess(Task t);
}
