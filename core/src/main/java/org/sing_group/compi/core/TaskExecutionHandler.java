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

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;

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
   *          the task that has been started. If the task is a loop, the
   *          concrete class will be {@link Foreach}
   */
  void taskStarted(Task task);

  /**
   * Called when a {@link Task} has been finished
   * 
   * If the task is a foreach loop, this is called once per loop
   * 
   * @param task
   *          the task that has been finished. If the task is a loop, the
   *          concrete class will be {@link Foreach}
   */
  void taskFinished(Task task);

  /**
   * Called when a {@link Task} has been aborted
   * 
   * If the task is a foreach loop, this is called once per loop
   * 
   * @param task
   *          The task that has been aborted. If the task is a loop, the
   *          concrete class will be {@link Foreach}
   * @param e
   *          The error that caused the task abortion
   */
  void taskAborted(Task task, CompiTaskAbortedException e);

  /**
   * Called when a {@link Foreach} iteration has started
   * 
   * @param iteration
   *          the iteration that has been started.
   */
  void taskIterationStarted(ForeachIteration iteration);

  /**
   * Called when a {@link Foreach} iteration has finished
   * 
   * @param iteration
   *          the iteration that has been finished.
   */
  void taskIterationFinished(ForeachIteration iteration);

  /**
   * Called when a {@link Foreach} iteration has aborted
   * 
   * @param iteration
   *          the iteration that has been finished.
   * @param e
   *          The error that caused the task abortion
   */
  void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e);
}
