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

import static org.sing_group.compi.core.loops.ForeachIteration.createIterationForForeach;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * Manages the {@link Task} execution and manages the {@link Task} dependencies
 * 
 */
public class TaskManager {

  private final Map<String, Task> tasksById = new ConcurrentHashMap<>();
  private final List<Task> tasksLeft = new CopyOnWriteArrayList<>();
  private final Map<Task, Set<Task>> dependencies = new ConcurrentHashMap<>();
  private final Map<Foreach, List<ForeachIteration>> forEachTasks = new ConcurrentHashMap<>();
  private VariableResolver variableResolver;

  /**
   * @param pipeline
   *          Indicates the {@link Pipeline}
   * @param resolver
   *          Indicates the {@link VariableResolver}
   */
  public TaskManager(final Pipeline pipeline, final VariableResolver resolver) {
    this.variableResolver = resolver;
    for (final Task task : pipeline.getTasks()) {
      this.tasksById.put(task.getId(), task);
      this.tasksLeft.add(task);
      this.dependencies.put(task, new HashSet<Task>());

      if (task instanceof Foreach) {
        this.forEachTasks.put((Foreach) task, new LinkedList<ForeachIteration>());
      }
    }
  }

  /**
   * Returns a {@link List} of {@link Task} ready to run. The first time, it
   * will check if the {@link Task} has dependencies or not. If the {@link Task}
   * doesn't have any dependency it can be executed, otherwise the {@link Task}
   * must wait until its dependencies has been finished
   * 
   * @return A {@link List} of {@link Task} ready to run
   */
  public List<Task> getRunnableTasks() {
    List<Task> runnableTasks = new ArrayList<Task>();
    for (final Task task : this.tasksLeft) {
      if (!task.isFinished() && checkTaskDependencies(task)) {
        runnableTasks.add(task);
      }
    }
    // }
    return runnableTasks;
  }

  /**
   * Goes through all the {@link Task} dependencies to check if they are
   * finished
   * 
   * @param task
   *          Indicates the {@link Task}
   * @return <code>true</code> if all its dependencies are finished,
   *         <code>false</code> otherwise
   */
  private boolean checkTaskDependencies(final Task task) {
    int count = 0;
    if (task.getAfter() == null)
      return true;
    final String[] dependsArray = task.getAfter().split(",");
    for (final String s : dependsArray) {
      final Task taskToCheck = tasksById.get(s);
      if (taskToCheck.isFinished()) {
        count++;
      }
    }
    if (count == dependsArray.length) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets a task given its id
   * 
   * @param taskId
   *          the task's Id
   * @return
   */
  public Task getTaskById(String taskId) {
    return tasksById.get(taskId);
  }

  /**
   * Getter of the tasksLeft attribute
   * 
   * @return The value of the tasksLeft attribute
   */
  public List<Task> getTasksLeft() {
    return tasksLeft;
  }

  /**
   * Gets the dependencies of a given task
   * 
   * @param t
   *          The task whose dependencies will be retrived
   * @return The dependencies of the given task
   */

  public Set<Task> getDependenciesOfTask(Task t) {
    return dependencies.get(t);
  }

  /**
   * Getter of the forEachTasks attribute
   * 
   * @return The value of the forEachTasks attribute
   */
  public List<ForeachIteration> getForeachIterations(Foreach foreach) {
    return forEachTasks.get(foreach);
  }

  /**
   * Creates the {@link ForeachIteration} to prepare the loop execution for a
   * task
   *
   * @param foreach
   *          The task to initialize
   *
   * @throws IllegalArgumentException
   *           If the directory contained in the source attribute doesn't have
   *           any file or if the element attribute contains a non existent
   *           value
   */
  public void initializeForEach(Foreach foreach) throws IllegalArgumentException {
    List<ForeachIteration> value = this.getForeachIterations(foreach);
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
        value.add(createIterationForForeach(foreach, source, index++));
      }
    }
  }

  /**
   * Initializes the {@link Task} dependencies. First it will check the
   * {@link Task} dependencies and then it will check if a {@link Task} it's a
   * dependency of another {@link Task}
   */
  public void initializeDependencies() {
    tasksById.values().forEach((task) -> {
      if (task.getAfter() != null) {
        for (final String afterId : task.getAfter().split(",")) {
          dependencies.get(tasksById.get(afterId)).add(task);
        }
      }
    });

    tasksById.values().forEach((task) -> {
      dependencies.forEach((otherTask, otherTaskDependencies) -> {
        if (otherTaskDependencies.contains(task)) {
          otherTaskDependencies.addAll(dependencies.get(task));
        }

        if (otherTaskDependencies.contains(otherTask)) {
          throw new IllegalArgumentException("The pipeline contains a cycle");
        }
      });
    });
  }

  /**
   * Marks all the {@link Task} as skipped if they are dependencies of the
   * {@link Task} passed as parameter
   * 
   * @param task
   *          Indicates the {@link Task} which you want to skip its dependencies
   */
  public void skipDependencies(final Task task) {
    dependencies.forEach((otherTask, otherTaskDependencies) -> {
      if (otherTaskDependencies.contains(task)) {
        otherTask.setSkipped(true);
      }
    });
  }

  /**
   * Marks all the {@link Task} as skipped if they are dependencies of the
   * {@link Task}
   * 
   * @param task
   *          Indicates the {@link Task} whose dependencies are not skipped
   */
  public void skipAllButDependencies(Task task) {
    this.tasksById.values().stream()
    .filter(t -> !getDependenciesOfTask(t).contains(task))
    .forEach(t -> t.setSkipped(true));
  }

  /**
   * Marks all the {@link Task} as skipped except the {@link Task} passed as
   * parameter
   * 
   * @param task
   *          Indicates the only {@link Task} ID which will not be skipped
   */
  public void skipAllTasksBut(Task task) {
    this.tasksById.values().stream()
      .filter(t -> !t.getId().equals(task.getId()))
      .forEach(t -> t.setSkipped(true));
  }

  synchronized public void setRunning(final Task task) {
    this.getTaskById(task.getId()).setRunning(true);
    this.getTasksLeft().remove(task);
  }

  synchronized public void setFinished(final Task task) {
    this.getTaskById(task.getId()).setFinished(true);
    this.getTaskById(task.getId()).setRunning(false);
  }

  public void setAborted(final Task task, final Exception e) {
    this.getTaskById(task.getId()).setAborted(true);
    this.getTaskById(task.getId()).setRunning(false);
    this.getTasksLeft().remove(task);
  }

}
