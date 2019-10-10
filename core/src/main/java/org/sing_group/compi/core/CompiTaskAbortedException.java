/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sing_group.compi.core;

import java.util.LinkedList;

import org.sing_group.compi.core.pipeline.Task;

/**
 * A class for representing exceptions thrown when a task aborts
 * 
 * This object gives access to the aborting {@link Task}, the underlying
 * {@link Exception} and the last lines of the aborting process's stdin and
 * stdout.
 * 
 */
public class CompiTaskAbortedException extends Exception {
  private static final long serialVersionUID = 1L;

  private Task task;
  private LinkedList<String> lastStdOut;
  private LinkedList<String> lastStdErr;

  public CompiTaskAbortedException(
    String message, Exception cause, Task t, LinkedList<String> lastStdOut, LinkedList<String> lastStdErr
  ) {
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
