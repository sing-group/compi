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

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.sing_group.compi.core.loops.ForeachIteration.createIterationForForeach;
import static org.sing_group.compi.core.runner.ProcessCreator.createProcess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.sing_group.compi.core.loops.CommandLoopValuesGenerator;
import org.sing_group.compi.core.loops.FileLoopValuesGenerator;
import org.sing_group.compi.core.loops.ForeachIteration;
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
  private final Map<Task, Set<Task>> dependants = new ConcurrentHashMap<>();
  private final Map<Foreach, List<ForeachIteration>> forEachTasks = new ConcurrentHashMap<>();
  private VariableResolver variableResolver;

  public TaskManager(final Pipeline pipeline, final VariableResolver resolver) {
    this.variableResolver = resolver;
    for (final Task task : pipeline.getTasks()) {
      this.tasksById.put(task.getId(), task);
      this.tasksLeft.add(task);
      this.dependants.put(task, new LinkedHashSet<Task>());

      if (task instanceof Foreach) {
        this.forEachTasks.put((Foreach) task, new LinkedList<ForeachIteration>());
      }
    }
    initializeDependencies();
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
      if (!task.isFinished() && dependenciesAreMet(task)) {
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
    return unmodifiableSet(dependants.get(task));
  }

  /**
   * Gets all tasks that a given task depends on
   * 
   * @param task
   *          The task whose dependencies will be obtained
   * @return Tasks that task depends on
   */
  public Set<Task> getDependenciesOfTask(Task task) {
    Set<Task> taskDependencies = new HashSet<>();
    dependants.forEach((otherTask, dependantTasks) -> {
      if (dependantTasks.contains(task)) {
        taskDependencies.add(otherTask);
      }
    });
    return taskDependencies;
  }

  /**
   * Get the iterations of a running foreach
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
    getDependenciesOfTask(task).forEach(dependency -> dependency.setSkipped(true));
  }

  /**
   * Marks all the tasks as skipped if they are not dependencies of a given task
   * 
   * @param task
   *          Task whose dependencies will be not skipped
   */
  public void skipAllButDependencies(Task task) {
    final Set<Task> dependenciesOfTask = getDependenciesOfTask(task);
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
    evaluateIf(task);
    if (!task.isSkipped() && task instanceof Foreach) {
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
  public void setAborted(final Task task, final Exception e) {
    task.setAborted(true);
    task.setRunning(false);
    this.tasksLeft.remove(task);
  }

  private boolean dependenciesAreMet(final Task task) {
    return this.getDependenciesOfTask(task).stream()
      .filter(t -> !t.isFinished())
      .collect(Collectors.toList())
      .size() == 0;
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
        this.forEachTasks.get(foreach).add(createIterationForForeach(foreach, source, index++));
      }
    }
  }

  private void initializeDependencies() {
    tasksById.values().forEach((task) -> {
      if (task.getAfter() != null) {
        for (final String afterId : task.getAfter().split("[\\s,]+")) {
          dependants.get(tasksById.get(afterId)).add(task);
        }
      }
    });

    tasksById.values().forEach((task) -> {
      dependants.forEach((otherTask, otherTaskDependants) -> {
        if (otherTaskDependants.contains(task)) {
          otherTaskDependants.addAll(dependants.get(task));
        }

        if (otherTaskDependants.contains(otherTask)) {
          throw new IllegalArgumentException("The pipeline contains a cycle");
        }
      });
    });
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
