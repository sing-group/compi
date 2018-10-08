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
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

/**
 * Manages the {@link Task} execution and manages the {@link Task} dependencies
 * 
 * @author Jesus Alvarez Casanova
 * @author Daniel Glez-Peña
 */
public class TaskManager {

  private final Map<String, Task> tasksById = new ConcurrentHashMap<>();
  private final List<String> tasksLeft = new CopyOnWriteArrayList<>();
  private final Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();
  private final Map<String, List<ForeachIteration>> forEachTasks = new ConcurrentHashMap<>();
  private VariableResolver variableResolver;

  /**
   * @param pipeline Indicates the {@link Pipeline}
   * @param resolver Indicates the {@link VariableResolver}
   */
  public TaskManager(final Pipeline pipeline, final VariableResolver resolver) {
    this.variableResolver = resolver;
    for (final Task p : pipeline.getTasks()) {
      this.tasksById.put(p.getId(), p);
      this.tasksLeft.add(p.getId());
      this.dependencies.put(p.getId(), new HashSet<String>());

      if (p instanceof Foreach) {
        this.forEachTasks.put(p.getId(), new LinkedList<ForeachIteration>());
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
    for (final String taskId : this.tasksLeft) {
      final Task task = tasksById.get(taskId);
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
   * @param task Indicates the {@link Task}
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
   * Getter of the DAG attribute
   * 
   * @return The value of the DAG attribute
   */
  public Map<String, Task> getTasksById() {
    return tasksById;
  }

  /**
   * Getter of the tasksLeft attribute
   * 
   * @return The value of the tasksLeft attribute
   */
  public List<String> getTasksLeft() {
    return tasksLeft;
  }

  /**
   * Getter of the dependencies attribute
   * 
   * @return The value of the dependencies attribute
   */
  public Map<String, Set<String>> getDependencies() {
    return dependencies;
  }

  /**
   * Getter of the forEachTasks attribute
   * 
   * @return The value of the forEachTasks attribute
   */
  public Map<String, List<ForeachIteration>> getForEachTasks() {
    return forEachTasks;
  }

  /**
   * Creates the {@link ForeachIteration} to prepare the loop execution for a
   * task
   *
   * @param foreach The task to initialize
   *
   * @throws IllegalArgumentException If the directory contained in the source
   *           attribute doesn't have any file or if the element attribute
   *           contains a non existent value
   */
  public void initializeForEach(Foreach foreach) throws IllegalArgumentException {
	  List<ForeachIteration> value = this.getForEachTasks().get(foreach.getId());
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
    tasksById.forEach((key, value) -> {
      if (value.getAfter() != null) {
        for (final String afterId : value.getAfter().split(",")) {
          dependencies.get(afterId).add(key);
        }
      }
    });

    tasksById.forEach((key, value) -> {
      dependencies.forEach((key2, value2) -> {
        if (value2.contains(key)) {
          value2.addAll(dependencies.get(key));
        }

        if (value2.contains(key2)) {
          throw new IllegalArgumentException("The pipeline contains a cycle");
        }
      });
    });
  }

  /**
   * Marks a {@link Task} as skipped
   * 
   * @param task Indicates the {@link Task} ID which you want to skip
   */
  public void skipTask(String task) {
    this.getTasksById().get(task).setSkipped(true);
  }

  /**
   * Marks a {@link Task} as not skipped
   * 
   * @param task Indicates the {@link Task} ID which you want not to skip
   */
  public void unSkipTask(String task) {
    this.getTasksById().get(task).setSkipped(false);
  }

  /**
   * Marks all the {@link Task} as skipped if they are dependencies of the
   * {@link Task} passed as parameter
   * 
   * @param task Indicates the {@link Task} ID which you want to skip its
   *          dependencies
   */
  public void skipDependencies(final String task) {
    dependencies.forEach((key, value) -> {
      if (value.contains(task)) {
        this.getTasksById().get(key).setSkipped(true);
      }
    });
  }

  /**
   * Marks all the {@link Task} as skipped if they are dependencies of the
   * {@link Task}
   * 
   * @param task Indicates the {@link Task} ID whose dependencies are not
   *          skipped
   */
  public void skipAllButDependencies(String task) {
    this.getTasksById().keySet().forEach((taskId) -> {
      if (!dependencies.get(taskId).contains(task)) {
        this.getTasksById().get(taskId).setSkipped(true);
      }
    });
  }

  /**
   * Marks all the {@link Task} as skipped except the {@link Task} passed as
   * parameter
   * 
   * @param task Indicates the only {@link Task} ID which will not be skipped
   */
  public void skipAllTasksBut(String task) {
    this.getTasksById().forEach((k, v) -> {
      if (!v.getId().equals(task)) {
        v.setSkipped(true);
      }
    });
  }

  synchronized public void setRunning(final Task task) {
    this.getTasksById().get(task.getId()).setRunning(true);
    this.getTasksLeft().remove(task.getId());
  }

  synchronized public void setFinished(final Task task) {
    this.getTasksById().get(task.getId()).setFinished(true);
    this.getTasksById().get(task.getId()).setRunning(false);
  }

  public void setAborted(final Task task, final Exception e) {
    this.getTasksById().get(task.getId()).setAborted(true);
    this.getTasksById().get(task.getId()).setRunning(false);
    this.getTasksLeft().remove(task.getId());
  }

}
