/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 *      López-Fernández, Jesús Álvarez Casanova
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

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.sing_group.compi.core.loops.ForeachIterationDependency;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;

public class TasksDAG {

  private final Map<Task, Set<Dependency<?>>> dag = new ConcurrentHashMap<>();

  public Set<Dependency<?>> getDependantsOfTask(Task t) {
    Set<Dependency<?>> dependantsOfTask = new HashSet<Dependency<?>>();
    if (this.dag.get(t)==null) {
      return new HashSet<>();
    }
   
    for (Dependency<?> dependency : this.dag.get(t)) {
      dependantsOfTask.add(dependency);
      Set<Dependency<?>> dependants = getDependantsOfTask(dependency.getDependantTask());
      dependantsOfTask.addAll(
        dependants.stream().map(d -> 
              new Dependency<Task>(t, d.getDependantTask())
        ).collect(Collectors.toSet())
      );
    }
    return dependantsOfTask;
  }

  /**
   * Gets all dependencies of a given task
   * 
   * @param task
   *          The task whose dependencies will be obtained
   * @return Tasks that task depends on
   */
  public Set<Dependency<?>> getDependenciesOfTask(Task task) {
    final Set<Dependency<?>> dependencies = new HashSet<>();
    dag.keySet().forEach(t -> {
      final Set<Dependency<?>> dependants = getDependantsOfTask(t);
      for (Dependency<?> dependency : dependants) {
        if (dependency.getDependantTask().equals(task)) {
          dependencies.add(dependency);
          break;
        }
      }
      ;
    });

    return dependencies;
  }

  public void initializeTaskDependencies(Collection<Task> tasks) {
    dag.clear();
    tasks.forEach(task -> {
      dag.put(task, new HashSet<>());
    });

    final Map<String, Task> tasksById = tasks.stream().collect(Collectors.toMap(Task::getId, identity()));
    tasks.forEach((task) -> {
      if (task.getAfter() != null) {
        for (final String afterId : task.getAfterList()) {
          boolean isIterationDependency = afterId.startsWith("*");
          final Task afterTask = tasksById.get(isIterationDependency ? afterId.substring(1) : afterId);
          // this.dag.get(afterTask).add(task);
          this.addDependency(afterTask, task, isIterationDependency);
        }
      }
    });
  }

  public boolean dependenciesAreMet(final Task task) {
    return this.getDependenciesOfTask(task).stream()
      .map(Dependency::getOnTask).filter(t -> !t.isFinished())
      .collect(Collectors.toList())
      .size() == 0;
  }

  public void removeDependency(Task t, Task dependant) {
    this.dag.get(t).removeIf(d -> d.getDependantTask().equals(dependant));
  }

  public void addDependency(Task task, Task dependant, boolean isIterationDependency) {
    if (getDependantsOfTask(dependant).stream().map(Dependency::getDependantTask).anyMatch(t -> t.equals(task))) {
      throw new IllegalArgumentException("The pipeline contains a cycle");
    }
    if (!this.dag.containsKey(task)) {
      this.dag.put(task, new HashSet<>());
    }
    this.dag.get(task).add(
      isIterationDependency ? new ForeachIterationDependency((Foreach) task, (Foreach) dependant) : new Dependency<Task>(task, dependant)
    );
  }
}
