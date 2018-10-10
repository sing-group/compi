/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.core;

import java.util.LinkedList;

import org.sing_group.compi.xmlio.entities.Task;

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
