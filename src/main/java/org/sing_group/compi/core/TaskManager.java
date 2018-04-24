package org.sing_group.compi.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sing_group.compi.core.loops.CommandLoopValuesGenerator;
import org.sing_group.compi.core.loops.FileLoopValuesGenerator;
import org.sing_group.compi.core.loops.ListLoopValuesGenerator;
import org.sing_group.compi.core.loops.LoopValuesGenerator;
import org.sing_group.compi.core.loops.LoopTask;
import org.sing_group.compi.core.loops.ParameterLoopValuesGenerator;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

/**
 * Manages the {@link Task} execution and manages the {@link Task}
 * dependencies
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class TaskManager implements TaskExecutionHandler {

	private final TaskExecutionHandler handler;
	private final Map<String, Task> DAG = new ConcurrentHashMap<>();
	private final List<String> tasksLeft = new CopyOnWriteArrayList<>();
	private final List<Task> runnableTasks = new CopyOnWriteArrayList<>();
	private final Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();
	private final Map<String, List<LoopTask>> forEachTasks = new ConcurrentHashMap<>();
	private boolean firstExecution;
	private VariableResolver variableResolver;

	/**
	 * @param handler
	 *            Indicates the {@link TaskExecutionHandler}
	 * @param pipeline
	 *            Indicates the {@link Pipeline}
	 */
	public TaskManager(final TaskExecutionHandler handler, final Pipeline pipeline, final VariableResolver resolver) {
		this.handler = handler;
		this.variableResolver = resolver;
		for (final Task p : pipeline.getTasks()) {
			this.DAG.put(p.getId(), p);
			this.tasksLeft.add(p.getId());
			this.dependencies.put(p.getId(), new HashSet<String>());

			if (p instanceof Foreach) {
				this.forEachTasks.put(p.getId(), new LinkedList<LoopTask>());
			}
		}
		this.firstExecution = true;
	}

	/**
	 * Returns a {@link List} of {@link Task} ready to run. The first time,
	 * it will check if the {@link Task} has dependencies or not. If the
	 * {@link Task} doesn't have any dependency it can be executed, otherwise
	 * the {@link Task} must wait until its dependencies has been finished
	 * 
	 * @return A {@link List} of {@link Task} ready to run
	 */
	public List<Task> getRunnableTasks() {
		this.runnableTasks.clear();
		if (this.firstExecution) {
			this.firstExecution = false;
			DAG.forEach((key, value) -> {
				if (value.getAfter() == null) {
					this.runnableTasks.add(value);
				}
			});
		} else {
			for (final String taskId : this.tasksLeft) {
				final Task task = DAG.get(taskId);
				if (checkTaskDependencies(task)) {
					this.runnableTasks.add(task);
				}
			}
		}
		return runnableTasks;
	}

	/**
	 * Goes through all the {@link Task} dependencies to check if they are
	 * finished
	 * 
	 * @param task
	 *            Indicates the {@link Task}
	 * @return <code>true</code> if all its dependencies are finished,
	 *         <code>false</code> otherwise
	 */
	private boolean checkTaskDependencies(final Task task) {
		int count = 0;
		final String[] dependsArray = task.getAfter().split(",");
		for (final String s : dependsArray) {
			final Task taskToCheck = DAG.get(s);
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
	 * Verifies that exist all the IDs contained in the {@link Task}
	 * attribute "after"
	 * 
	 * @throws IllegalArgumentException
	 *             If the {@link Task} ID contained in the after
	 *             attribute doesn't exist
	 */
	public void checkAfterIds() throws IllegalArgumentException {
		for (final String tasks : this.tasksLeft) {
			final Task task = DAG.get(tasks);
			if (task.getAfter() != null) {
				for (final String afterId : task.getAfter().split(",")) {
					if (!DAG.containsKey(afterId)) {
						throw new IllegalArgumentException(
								"The IDs contained in the after attribute of the task " + task.getId()
										+ " aren't correct");
					}
				}
			}
		}
	}

	/**
	 * Creates the {@link LoopTask} to prepare the loop execution for a task
	 *
	 * @param foreach The task to initialize
	 *
	 * @throws IllegalArgumentException
	 *             If the directory contained in the source attribute doesn't
	 *             have any file or if the element attribute contains a non
	 *             existent value
	 */
	public void initializeForEach(Foreach foreach) throws IllegalArgumentException {
		if (this.getForEachTasks().containsKey(foreach.getId())) {
			List<LoopTask> value = this.getForEachTasks().get(foreach.getId());
			List<String> values = new LinkedList<>();

			LoopValuesGenerator generator = null;
			switch (foreach.getOf()) {
				case "list":
					generator = new ListLoopValuesGenerator();
					break;
				case "file":
					generator = new FileLoopValuesGenerator();
					
					break;
				case "param":
					generator = new ParameterLoopValuesGenerator(this.variableResolver);
					break;
				case "command":
					generator = new CommandLoopValuesGenerator(this.variableResolver);
					break;
				default:
					throw new IllegalArgumentException("The element " + foreach.getOf()
							+ " of the task " + foreach.getId() + " doesn't exist");
			}
			
			values = generator.getValues(foreach.getIn());

			for (final String source : values) {
				value.add(new LoopTask(foreach.getExec(), source, foreach.getAs()));
			}
		}
	}

	/**
	 * Initializes the {@link Task} dependencies. First it will check the
	 * {@link Task} dependencies and then it will check if a {@link Task}
	 * it's a dependency of another {@link Task}
	 */
	public void initializeDependencies() {
		DAG.forEach((key, value) -> {
			if (value.getAfter() != null) {
				for (final String afterId : value.getAfter().split(",")) {
					dependencies.get(afterId).add(key);
				}
			}
		});

		DAG.forEach((key, value) -> {
			dependencies.forEach((key2, value2) -> {
				if (value2.contains(key)) {
					value2.addAll(dependencies.get(key));
				}

				if (value2.contains(key2)) {
					throw new IllegalArgumentException ("The pipeline contains a cycle");
				}
			});
		});
	}

	/**
	 * Marks all the {@link Task} as skipped if they are dependencies of the
	 * {@link Task} passed as parameter
	 * 
	 * @param advanceTo
	 *            Indicates the {@link Task} ID which you want to advance
	 */
	public void skipTasks(final String advanceTo) {
		dependencies.forEach((key, value) -> {
			if (value.contains(advanceTo)) {
				this.getDAG().get(key).setSkipped(true);
			}
		});
	}
	
	
	/**
	 * Marks all the {@link Task} as skipped except the
	 * {@link Task} passed as parameter
	 * 
	 * @param singleTask
	 *            Indicates the {@link Task} ID to run
	 */
	public void skipAllTasksBut(String singleTask) {
		this.getDAG().forEach((k, v)->{
			if (!v.getId().equals(singleTask)) {
				v.setSkipped(true);
			}
			
		});
	}

	/**
	 * Marks a {@link Task} as started
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been started
	 */
	@Override
	public void taskStarted(final Task task) {
		this.getDAG().get(task.getId()).setRunning(true);
		this.getTasksLeft().remove(task.getId());
	}

	/**
	 * Marks a {@link Task} as finished
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been started
	 */
	@Override
	public void taskFinished(final Task task) {
		this.getDAG().get(task.getId()).setFinished(true);
		this.getDAG().get(task.getId()).setRunning(false);
	}

	/**
	 * Marks a {@link Task} as aborted and aborts all the {@link Task}
	 * which has as dependency of the aborted {@link Task}
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	@Override
	public void taskAborted(final Task task, final Exception e) {
		this.getDAG().get(task.getId()).setAborted(true);
		this.getDAG().get(task.getId()).setRunning(false);
		for (final String taskToAbort : this.getDependencies().get(task.getId())) {
			if (this.getTasksLeft().contains(taskToAbort)) {
				handler.taskAborted(this.getDAG().get(taskToAbort), e);
				this.getTasksLeft().remove(taskToAbort);
			}
		}
	}

	/**
	 * Getter of the DAG attribute
	 * 
	 * @return The value of the DAG attribute
	 */
	public Map<String, Task> getDAG() {
		return DAG;
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
	public Map<String, List<LoopTask>> getForEachTasks() {
		return forEachTasks;
	}



}
