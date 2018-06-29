package org.sing_group.compi.core;

import org.sing_group.compi.xmlio.entities.Task;

/**
 * Indicates when a {@link Task} is started/finished/aborted
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public interface TaskExecutionHandler {
	/**
	 * Indicates when a {@link Task} has been started
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been started
	 */
	void taskStarted(Task task);

	/**
	 * Indicates when a {@link Task} has been finished
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been finished
	 */
	void taskFinished(Task task);

	/**
	 * Indicates when a {@link Task} has been aborted
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	void taskAborted(Task task, Exception e);
}
