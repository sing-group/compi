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

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toSet;
import static org.sing_group.compi.core.loops.ForeachIteration.createIterationForForeach;
import static org.sing_group.compi.core.runner.ProcessCreator.createProcess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sing_group.compi.core.loops.CommandLoopValuesGenerator;
import org.sing_group.compi.core.loops.FileLoopValuesGenerator;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.loops.ForeachIterationDependency;
import org.sing_group.compi.core.loops.ListLoopValuesGenerator;
import org.sing_group.compi.core.loops.LoopValuesGenerator;
import org.sing_group.compi.core.loops.ParameterLoopValuesGenerator;
import org.sing_group.compi.core.loops.RangeLoopValuesGenerator;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * A class to assist {@link CompiApp} in pipeline execution
 * 
 * This class facilitates dependency-based operations for (i) running a pipeline
 * meting task dependency restrictions and (ii) enabling or disabling pipeline
 * paths based on task dependencies.
 * 
 */
public class TaskManager {

  private final Map<String, Task> tasksById = new ConcurrentHashMap<>();
  private final List<Task> tasksLeft = new CopyOnWriteArrayList<>();
  private final TasksDAG dag = new TasksDAG();
  private final Map<Foreach, List<ForeachIteration>> forEachTasks = new ConcurrentHashMap<>();
  private VariableResolver variableResolver;

  public TaskManager(final Pipeline pipeline, final VariableResolver resolver) {
    this.variableResolver = resolver;
    for (final Task task : pipeline.getTasks()) {
      this.tasksById.put(task.getId(), task);
      this.tasksLeft.add(task);
      // this.dependants.put(task, new LinkedHashSet<Task>());

      if (task instanceof Foreach) {
        this.forEachTasks.put((Foreach) task, new LinkedList<ForeachIteration>());
      }
    }
    this.dag.initializeTaskDependencies(pipeline.getTasks());
  }

  /**
   * Gets a task given its id
   * 
   * @param taskId
   *          The task Id
   * @return The task
   */
  public Task getTaskById(String taskId) {
    return tasksById.get(taskId);
  }

  /**
   * Get a list of tasks that were not run yet
   * 
   * @return Task that were not run yet
   */
  public List<Task> getTasksLeft() {
    return unmodifiableList(tasksLeft);
  }

  /**
   * Get a list of {@link Task} which are ready to run, that is, tasks that are
   * not finished and their dependencies are met
   * 
   * @return List of {@link Task} ready to run
   */
  public List<Task> getRunnableTasks() {
    List<Task> runnableTasks = new ArrayList<Task>();
    for (final Task task : this.tasksLeft) {
      if (!task.isFinished() && this.dag.dependenciesAreMet(task)) {
        runnableTasks.add(task);
      }
    }
    return runnableTasks;
  }

  /**
   * Gets the tasks that depends on a given task
   * 
   * @param task
   *          The task whose dependant tasks will be obtained
   * @return The dependant tasks of the given task
   */

  public Set<Task> getDependantTasks(Task task) {
    return dag.getDependantsOfTask(task).stream().map(Dependency::getDependantTask).collect(toSet());
  }

  /**
   * Get the iterations of a running foreach
   *
   * @param foreach
   *          the running foreach
   * 
   * @return The iterations of the given foreach
   * @throws IllegalStateException
   *           if the foreach is not running yet. It should running before
   *           calling this method (call {@link TaskManager#setRunning(Task)}
   *           before
   */
  public List<ForeachIteration> getForeachIterations(Foreach foreach) {
    if (!foreach.isRunning()) {
      throw new IllegalStateException("Foreach is not running yet");
    }
    return unmodifiableList(forEachTasks.get(foreach));
  }

  /**
   * Marks all the tasks as skipped if they are dependencies of a given task
   * 
   * @param task
   *          Task whose dependencies will be skipped
   */
  public void skipDependencies(final Task task) {
    dag.getDependenciesOfTask(task).stream().map(Dependency::getOnTask)
      .forEach(dependency -> dependency.setSkipped(true));
  }

  /**
   * Marks all the tasks as skipped if they are not dependencies of a given task
   * 
   * @param task
   *          Task whose dependencies will be not skipped
   */
  public void skipAllButDependencies(Task task) {
    final Set<Task> dependenciesOfTask =
      dag.getDependenciesOfTask(task).stream().map(Dependency::getOnTask).collect(toSet());
    this.tasksById.values().stream()
      .filter(t -> !dependenciesOfTask.contains(t))
      .forEach(t -> t.setSkipped(true));
  }

  /**
   * Marks all the tasks as skipped except a given task
   * 
   * @param task
   *          The only task that will to be skipped
   */
  public void skipAllTasksBut(Task task) {
    this.tasksById.values().stream()
      .filter(t -> !t.equals(task))
      .forEach(t -> t.setSkipped(true));
  }

  /**
   * Sets a task as running
   * 
   * The task will be removed from the left tasks to run. If the task is a
   * foreach loop, its iteration values will be calculated
   * 
   * @param task
   *          The task to pass into a running state
   */
  public void setRunning(final Task task) {
    task.setRunning(true);
    if (!task.isSkipped())
      evaluateIf(task);
    if (!task.isSkipped() && task instanceof Foreach && !(task instanceof ForeachIteration)) {
      initializeForEach((Foreach) task);
    }
    this.tasksLeft.remove(task);
  }

  /**
   * Marks a task as correctly finished
   * 
   * @param task
   *          The task to mark as finished
   */
  public void setFinished(final Task task) {
    task.setFinished(true);
    task.setRunning(false);
  }

  /**
   * Marks a task a incorrectly finished (aborted)
   * 
   * @param task
   *          The task to mark as aborted
   * @param e
   *          The cause of the abortion
   */
  public void setAborted(final Task task, final CompiTaskAbortedException e) {
    task.setAborted(true, e);
    task.setRunning(false);
    this.tasksLeft.remove(task);
  }

  private void initializeForEach(Foreach foreach) throws IllegalArgumentException {
    List<String> values = new LinkedList<>();

    LoopValuesGenerator generator = null;
    switch (foreach.getOf()) {
      case "list":
        generator = new ListLoopValuesGenerator(this.variableResolver, foreach);
        break;
      case "range":
        generator = new RangeLoopValuesGenerator(this.variableResolver, foreach);
        break;
      case "file":
        generator = new FileLoopValuesGenerator(this.variableResolver, foreach);
        break;
      case "param":
        generator = new ParameterLoopValuesGenerator(this.variableResolver, foreach);
        break;
      case "command":
        generator = new CommandLoopValuesGenerator(this.variableResolver, foreach);
        break;
      default:
        throw new IllegalArgumentException(
          "The element " + foreach.getOf()
            + " of the task " + foreach.getId() + " doesn't exist"
        );
    }

    if (!foreach.isSkipped()) {
      values = generator.getValues(foreach.getIn());
      int index = 0;
      for (final String source : values) {
        ForeachIteration iteration = createIterationForForeach(foreach, source, index++);
        this.tasksLeft.add(iteration);
        this.forEachTasks.get(foreach).add(iteration);
      }
      this.dag.getDependantsOfTask(foreach).stream().forEach(dependency -> {
        final Task dependant = dependency.getDependantTask();
        this.dag.removeDependency(foreach, dependant);
        if (dependency instanceof ForeachIterationDependency && !dependant.isSkipped()) {
          // iteration-binded loop
          if (!dependant.isRunning())
            setRunning(dependant); // here, the foreach values of dependant loop
                                   // are computed

            List<ForeachIteration> taskIterations = this.getForeachIterations((Foreach) dependency.getOnTask());
            List<ForeachIteration> dependantIterations = this.getForeachIterations((Foreach) dependant);
            
            if (dependantIterations.size() != taskIterations.size()) {
              throw new IllegalArgumentException("Iteration values of foreach "+foreach.getId()+" has a different number of elements of dependant foreach "+dependant.getId());
            }
            for (int i = 0; i < Math.min(taskIterations.size(), dependantIterations.size()); i++) {
              this.dag.addDependency(taskIterations.get(i), dependantIterations.get(i), false);
            }
        } else {
          // regular loop
          this.forEachTasks.get(foreach).forEach(foreachTask -> {

            this.dag.addDependency(foreachTask, dependant, false);

          });
        }
      });
    }
  }

  private void evaluateIf(Task task) {
    if (task.getRunIf() != null) {
      try {
        Process runIfProcess = createProcess(task.getRunIf(), task, this.variableResolver);
        runIfProcess.waitFor();
        if (runIfProcess.exitValue() != 0) {
          task.setSkipped(true);
        }

      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
