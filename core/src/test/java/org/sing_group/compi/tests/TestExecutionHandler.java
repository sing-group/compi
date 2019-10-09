/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sing_group.compi.core.CompiTaskAbortedException;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;

public class TestExecutionHandler implements TaskExecutionHandler {
  final List<String> startedTasks = new ArrayList<>();
  final List<String> finishedTasks = new ArrayList<>();
  final List<String> finishedTasksIncludingLoopChildren = new ArrayList<>();
  final List<String> abortedTasks = new ArrayList<>();
  final Set<String> startedForeachs = new HashSet<>();

  @Override
  synchronized public void taskStarted(Task task) {
    startedTasks.add(task.getId());
  }

  @Override
  public void taskFinished(Task task) {
    finishedTasks.add(task.getId());
    if (!(task instanceof Foreach))
      finishedTasksIncludingLoopChildren.add(task.getId());
  }

  @Override
  public void taskAborted(Task task, CompiTaskAbortedException e) {
    abortedTasks.add(task.getId());
  }

  public List<String> getStartedTasks() {
    return startedTasks;
  }

  public List<String> getFinishedTasks() {
    return finishedTasks;
  }

  public List<String> getAbortedTasks() {
    return abortedTasks;
  }

  public List<String> getFinishedTasksIncludingLoopChildren() {
    return finishedTasksIncludingLoopChildren;
  }

  @Override
  public void taskIterationStarted(ForeachIteration iteration) {
    startedForeachs.add(iteration.getParentForeachTask().getId());

  }

  @Override
  public void taskIterationFinished(ForeachIteration iteration) {
    finishedTasksIncludingLoopChildren.add(iteration.getParentForeachTask().getId());
  }

  @Override
  public void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {}
}
