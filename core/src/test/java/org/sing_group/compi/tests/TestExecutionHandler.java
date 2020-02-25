/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
  final List<String> loopIterations = new ArrayList<>();

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

  public List<String> getLoopIterations() {
    return loopIterations;
  }

  public List<String> getFinishedTasksIncludingLoopChildren() {
    return finishedTasksIncludingLoopChildren;
  }

  @Override
  public void taskIterationStarted(ForeachIteration iteration) {
    startedForeachs.add(iteration.getParentForeachTask().getId());
    loopIterations.add("S_" + iteration.getParentForeachTask().getId() + "_" + iteration.getIterationIndex());
  }

  @Override
  public void taskIterationFinished(ForeachIteration iteration) {
    finishedTasksIncludingLoopChildren.add(iteration.getParentForeachTask().getId());
    loopIterations.add("E_" + iteration.getParentForeachTask().getId() + "_" + iteration.getIterationIndex());
  }

  @Override
  public void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {}
}
