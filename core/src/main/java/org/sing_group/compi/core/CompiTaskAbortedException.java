package org.sing_group.compi.core;

import java.util.LinkedList;

import org.sing_group.compi.xmlio.entities.Task;

public class CompiTaskAbortedException extends Exception {
  private static final long serialVersionUID = 1L;
  
  private Task task;
  private LinkedList<String> lastStdOut;
  private LinkedList<String> lastStdErr;

  public CompiTaskAbortedException(String message, Exception cause, Task t, LinkedList<String> lastStdOut, LinkedList<String> lastStdErr) {
    super(message, cause);
    
    this.task = t;
    this.lastStdOut = lastStdOut;
    this.lastStdErr = lastStdErr;
  }
  
  public Task getTask() {
    return task;
  }
  
  public LinkedList<String> getLastStdOut() {
    return lastStdOut;
  }
  
  public LinkedList<String> getLastStdErr() {
    return lastStdErr;
  }
}
