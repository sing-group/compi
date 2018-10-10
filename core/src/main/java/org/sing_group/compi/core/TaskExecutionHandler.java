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
