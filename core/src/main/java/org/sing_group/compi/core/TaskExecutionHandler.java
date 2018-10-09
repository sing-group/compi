package org.sing_group.compi.core;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;

/**
 * An interface to listen to pipeline task's execution lifecycle
 * 
 * You can add objects of this interface in
 * {@link CompiApp#addTaskExecutionHandler(TaskExecutionHandler)}
 * 
 */
public interface TaskExecutionHandler {
	
	/**
	 * Called when a {@link Task} has been started
	 * 
	 * If the task is a foreach loop, this is called once per loop
	 * 
	 * @param task
	 *            the task that has been started. If the task
	 *            is a loop, the concrete class will be {@link Foreach}
	 */
	void taskStarted(Task task);
	
	/**
	 * Called when a {@link Task} has been finished
	 * 
	 * If the task is a foreach loop, this is called once per loop
	 * 
	 * @param task
	 *            the task that has been finished. If the task
	 *            is a loop, the concrete class will be {@link Foreach}
	 */
	void taskFinished(Task task);

	/**
	 * Called when a {@link Task} has been aborted
	 * 
	 * If the task is a foreach loop, this is called once per loop
	 * 
	 * @param task
	 *            The task that has been aborted. If the task
	 *            is a loop, the concrete class will be {@link Foreach}
	 * @param e
	 * 			  The error that caused the task abortion
	 */
	void taskAborted(Task task, CompiTaskAbortedException e);
	
	/**
	 * Called when a {@link Foreach} iteration has started
	 * 
	 * @param iteration
	 *            the iteration that has been started.
	 */
	void taskIterationStarted(ForeachIteration iteration);

	/**
	 * Called when a {@link Foreach} iteration has finished
	 * 
	 * @param iteration
	 *            the iteration that has been finished.
	 */
	void taskIterationFinished(ForeachIteration iteration);

	/**
	 * Called when a {@link Foreach} iteration has aborted
	 * 
	 * @param iteration
	 *            the iteration that has been finished.
	 * @param e
	 * 			  The error that caused the task abortion
	 */
	void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e);
}
