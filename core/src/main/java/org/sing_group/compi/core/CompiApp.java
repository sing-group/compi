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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.runner.RunnersManager;
import org.sing_group.compi.xmlio.ParamsFileVariableResolver;
import org.xml.sax.SAXException;

/**
 * This class is the entry point for running Compi pipelines
 * 
 * Instances can be created by using {@link CompiRunConfiguration} objects,
 * which include all parameters for the execution.
 * 
 * It is possible to subscribe to task execution events by using
 * {@link CompiApp#addTaskExecutionHandler(TaskExecutionHandler)}
 * 
 */
public class CompiApp {

  private final CompiRunConfiguration config;

  private final Pipeline pipeline;
  private final Map<Task, AtomicInteger> loopCounterOfTask = new HashMap<>();
  private final List<TaskExecutionHandler> executionHandlers = new ArrayList<>();
  private final CompiExecutionHandler executionHandler = new CompiExecutionHandler();

  private TaskManager taskManager;
  private VariableResolver resolver;
  private ExecutorService executorService;
  private RunnersManager runnersManager;

  private Object syncMonitor = this;

  /**
   * Returns the current compi project version
   * 
   * @return The Compi version
   */
  public static String getCompiVersion() {
    try {
      Properties p = new Properties();
      p.load(CompiApp.class.getResourceAsStream("/compi.version"));
      return p.getProperty("compi.version").toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a Compi application for running a pipeline. The configuration of
   * the execution is provided in a {@link CompiRunConfiguration} object.
   * 
   * @param config
   *          The configuration of the execution
   *
   * @throws IllegalArgumentException
   *           If some of the configuration parameters are wrong
   * @throws IOException
   *           If there is a problem accessing pipeline files, parameter files,
   *           etc.
   */
  public CompiApp(
    CompiRunConfiguration config
  ) throws IllegalArgumentException, IOException {

    validateParameters(config);

    this.config = config;

    this.pipeline = config.getPipeline();

    if (config.getParamsFile() != null) {
      this.resolver = new ParamsFileVariableResolver(config.getParamsFile());
    } else {
      this.resolver = config.getResolver();
    }

    this.taskManager = new TaskManager(this.pipeline, this.resolver);

    initializeRunnersManager();

    disableTasksThatWillNotRun();

    initializeExecutorService();
  }

  /**
   * Adds a {@link TaskExecutionHandler}
   * 
   * @param handler
   *          Indicates the {@link TaskExecutionHandler}
   */
  public void addTaskExecutionHandler(final TaskExecutionHandler handler) {
    this.executionHandlers.add(handler);
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  /**
   * Executes all the {@link Task} in an {@link ExecutorService}. When a
   * {@link Task} is executed, this thread will wait until the {@link Task}
   * notifies when it's finished or aborted
   * 
   * @throws SAXException
   *           If there is an error in the XML parsing
   * @throws IOException
   *           If an I/O exception of some sort has occurred
   * @throws IllegalArgumentException
   *           If there is an error in the XML pipeline/params file
   * @throws InterruptedException
   *           If there is an error while the thread is waiting
   * @throws ParserConfigurationException
   *           If there is a configuration error
   */
  public void run()
    throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, InterruptedException {

    synchronized (syncMonitor) {
      while (!taskManager.getTasksLeft().isEmpty()) {
        for (final Task taskToRun : taskManager.getRunnableTasks()) {
          try {
            taskManager.setRunning(taskToRun);
          } catch (RuntimeException e) {
            taskManager.setAborted(taskToRun, e);
            executorService.submit(()-> {
            this.executionHandler.taskAborted(
              taskToRun,
              new CompiTaskAbortedException(
                "Task aborted before being initialized: "+e.getMessage(), e, taskToRun, new LinkedList<String>(), new LinkedList<>()
              )
            );
            });
            continue;
          }
          if (taskToRun instanceof Foreach) {
            loopCounterOfTask.put(
              taskToRun, new AtomicInteger(
                taskManager.getForeachIterations((Foreach) taskToRun).size()
              )
            );
            if (taskManager.getForeachIterations((Foreach) taskToRun).size() == 0) {
              // for loops without iterations, we need to add a dummy process,
              // because we expect the foreach task itself
              // is in taskLeft and we need to be notified that it has finished
              executorService.submit(
                new TaskRunnable(
                  createIterationForForeach((Foreach) taskToRun, null, 0), this.executionHandler,
                  (task) -> {
                    return new DummyProcess();
                  }, null, null, false, this.config.isShowStdOuts()
                  )
                );
                } else {
              for (final ForeachIteration lp : taskManager.getForeachIterations((Foreach) taskToRun)) {
                final File stdOut = getLogFile(lp, ".out.log");
                final File stdErr = getLogFile(lp, ".err.log");

                executorService.submit(
                  new TaskRunnable(
                    lp, this.executionHandler, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()),
                    stdOut, stdErr, false, this.config.isShowStdOuts()
                    )
                  );
                  }
            }
          } else {
            final File stdOut = getLogFile(taskToRun, ".out.log");
            final File stdErr = getLogFile(taskToRun, ".err.log");
            executorService.submit(
              new TaskRunnable(
                taskToRun, this.executionHandler, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()),
                stdOut, stdErr, false, this.config.isShowStdOuts()
                )
              );
          }
        }
        syncMonitor.wait();
      }
    }

    executorService.shutdown();

    // wait for remaining tasks that are currently running
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
  }

  private void validateParameters(CompiRunConfiguration config) {
    boolean singleTaskIsPresent = config.getSingleTask() != null;
    boolean anyTaskExecutionModifierIsPresent =
      config.getFromTasks() != null || config.getAfterTasks() != null || config.getUntilTask() != null
        || config.getBeforeTask() != null;

    if (singleTaskIsPresent && anyTaskExecutionModifierIsPresent) {
      throw new IllegalArgumentException("singleTask is incompatible with any of fromTask, untilTask and beforeTask");
    }

    if (config.getUntilTask() != null && config.getBeforeTask() != null) {
      throw new IllegalArgumentException("untilTask is incompatible with beforeTask");
    }

    if (config.getAfterTasks() != null && config.getFromTasks() != null) {
      Set<String> afterTasksSet = new HashSet<>(config.getAfterTasks());
      afterTasksSet.retainAll(new HashSet<>(config.getFromTasks()));
      if (afterTasksSet.size() > 0) {
        throw new IllegalArgumentException("afterTasks and untilTasks cannot have tasks in common");
      }
    }

    if (config.getLogOnlyTasks() != null && config.getDoNotLogTasks() != null) {
      throw new IllegalArgumentException("logOnlyTasks is incompatible with doNotLogTasks");
    }
    if ((config.getLogOnlyTasks() != null || config.getDoNotLogTasks() != null) && config.getLogsDir() == null) {
      throw new IllegalArgumentException("logDir cannot be null if logOnlyTasks or doNotLogTasks are not null");
    }
  }

  private File getLogFile(final Task task, String suffix) {
    final File stdOut =
      this.config.getLogsDir() != null &&
        ((this.config.getLogOnlyTasks() != null && this.config.getLogOnlyTasks().contains(task.getId())) ||
          (this.config.getDoNotLogTasks() != null && !this.config.getDoNotLogTasks().contains(task.getId())) ||
          (this.config.getDoNotLogTasks() == null && this.config.getLogOnlyTasks() == null))
            ? new File(
              this.config.getLogsDir() + File.separator + task.getId()
                + (task instanceof ForeachIteration ? "_" + ((ForeachIteration) task).getIterationIndex() : "") + suffix
              )
            : null;
    return stdOut;
  }

  private void initializeRunnersManager() throws IOException {
    if (config.getRunnersFile() != null) {
      this.runnersManager = new RunnersManager(config.getRunnersFile(), this.pipeline, this.resolver);
    } else {
      this.runnersManager = new RunnersManager(this.resolver);
    }
  }

  private void disableTasksThatWillNotRun() {
    if (config.getSingleTask() != null) {
      skipAllBut(config.getSingleTask());
    } else {
      if (config.getFromTasks() != null) {
        for (String fromTask : config.getFromTasks()) {
          skipTasksBefore(fromTask);
        }
      }
      if (config.getAfterTasks() != null) {
        for (String afterTask : config.getAfterTasks()) {
          skipTasksBefore(afterTask);
          skipTask(afterTask);
        }
      }
      if (config.getUntilTask() != null) {
        runUntil(config.getUntilTask());
      } else if (config.getBeforeTask() != null) {
        runBefore(config.getBeforeTask());
      }
    }
  }

  /**
   * Initializes the {@link ExecutorService}
   * 
   * @param threadNumber
   *          Indicates the number of threads of the {@link ExecutorService}
   * @throws IllegalArgumentException
   *           If the number of threads is equal or less than 0 or if the number
   *           is a string instead of a number
   */
  private void initializeExecutorService() throws IllegalArgumentException {
    if (config.getMaxTasks() <= 0) {
      throw new IllegalArgumentException("The thread number must be higher than 0");
    } else {
      executorService = Executors.newFixedThreadPool(config.getMaxTasks());
    }
  }

  /**
   * Skips {@link Task}
   * 
   * @param taskId
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipTask(final String taskId) throws IllegalArgumentException {
    Task task = taskManager.getTaskById(taskId);
    if (!taskManager.getTasksLeft().contains(task)) {
      throw new IllegalArgumentException("The task ID " + taskId + " doesn't exist");
    } else {
      task.setSkipped(true);
      ;
    }
  }

  /**
   * Skips {@link Task} until the {@link Task} where you want to start
   * 
   * @param advanceToTaskId
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipTasksBefore(final String advanceToTaskId) throws IllegalArgumentException {
    Task advanceToTask = taskManager.getTaskById(advanceToTaskId);
    if (!taskManager.getTasksLeft().contains(advanceToTask)) {
      throw new IllegalArgumentException("The task ID " + advanceToTaskId + " doesn't exist");
    } else {
      taskManager.skipDependencies(advanceToTask);
    }
  }

  /**
   * Skips all {@link Task} but {@link Task}
   * 
   * @param singleTaskId
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipAllBut(String singleTaskId) {
    Task singleTask = taskManager.getTaskById(singleTaskId);
    if (!taskManager.getTasksLeft().contains(singleTask)) {
      throw new IllegalArgumentException("The task ID " + singleTaskId + " doesn't exist");
    } else {
      taskManager.skipAllTasksBut(singleTask);
    }
  }

  /**
   * Runs until {@link Task} including its dependencies
   * 
   * @param singleTask
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void runUntil(String untilTaskId) {
    Task untilTask = taskManager.getTaskById(untilTaskId);
    if (!taskManager.getTasksLeft().contains(untilTask)) {
      throw new IllegalArgumentException("The task ID " + untilTaskId + " doesn't exist");
    } else {
      taskManager.skipAllButDependencies(untilTask);
      untilTask.setSkipped(false);
    }
  }

  /**
   * Runs all dependencies of a {@link Task}, excluding the task itself
   * 
   * @param singleTask
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void runBefore(String beforeTaskId) {
    Task beforeTask = taskManager.getTaskById(beforeTaskId);
    if (!taskManager.getTasksLeft().contains(beforeTask)) {
      throw new IllegalArgumentException("The task ID " + beforeTaskId + " doesn't exist");
    } else {
      taskManager.skipAllButDependencies(beforeTask);
    }
  }

  private class CompiExecutionHandler implements TaskExecutionHandler {

    private Set<Foreach> foreachStartNotificationsSent = new HashSet<>();
    private Set<Foreach> foreachAbortedNotificationsSent = new HashSet<>();

    /**
     * Indicates that a {@link Task} is started
     * 
     * @param task
     *          Indicates the {@link Task} which has been started
     */
    @Override
    public void taskStarted(final Task task) {
      if (!task.isSkipped())
        this.notifyTaskStarted(task);
    }

    /**
     * Indicates that a {@link Task} is finished and notifies
     * 
     * @param task
     *          Indicates the {@link Task} which has been started
     */
    @Override
    public void taskFinished(final Task task) {
      synchronized (syncMonitor) {
        taskManager.setFinished(task);
        syncMonitor.notify();
        if (!task.isSkipped()) {
          this.notifyTaskFinished(task);
        }
      }
    }

    /**
     * Indicates that a {@link Task} is aborted and notifies
     * 
     * @param task
     *          Indicates the {@link Task} which has been aborted
     * @param e
     *          Indicates the {@link Exception} which causes the error
     */
    @Override
    public void taskAborted(final Task task, final CompiTaskAbortedException e) {
      synchronized (syncMonitor) {
        notifyTaskAborted(task, e);
        taskManager.setAborted(task, e);
        abortDependencies(task, e);
        syncMonitor.notify();
      }
    }

    @Override
    public void taskIterationStarted(ForeachIteration iteration) {
      synchronized (syncMonitor) {
        if (!foreachStartNotificationsSent.contains(iteration.getParentForeachTask())) {
          this.notifyTaskStarted(iteration.getParentForeachTask());
          foreachStartNotificationsSent.add(iteration.getParentForeachTask());
        }
        if (!iteration.isSkipped())
          this.notifyTaskIterationStarted(iteration);
      }
    }

    @Override
    public void taskIterationFinished(ForeachIteration iteration) {
      synchronized (syncMonitor) {
        final Task parent = iteration.getParentForeachTask();
        if (loopCounterOfTask.get(parent).decrementAndGet() <= 0) {
          taskManager.setFinished(parent);
          if (!iteration.isSkipped()) {
            this.notifyTaskFinished(iteration.getParentForeachTask());
          }
          syncMonitor.notify();
        }
        if (!iteration.isSkipped()) {
          this.notifyTaskIterationFinished(iteration);
        }
      }
    }

    @Override
    public void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {
      synchronized (syncMonitor) {
        this.notifyTaskIterationAborted(iteration, e);
        if (!foreachAbortedNotificationsSent.contains(iteration.getParentForeachTask())) {
          this.notifyTaskAborted(iteration.getParentForeachTask(), e);
          taskManager.setAborted(iteration.getParentForeachTask(), e);
          abortDependencies(iteration.getParentForeachTask(), e);
          foreachAbortedNotificationsSent.add(iteration.getParentForeachTask());
          syncMonitor.notify();
        }
      }
    }

    private void abortDependencies(Task task, CompiTaskAbortedException e) {
      for (final Task taskToAbort : taskManager.getDependantTasks(task)) {
        if (taskManager.getTasksLeft().contains(taskToAbort)) {
          if (!taskToAbort.isSkipped()) {
            notifyTaskAborted(
              taskToAbort,
              new CompiTaskAbortedException(
                "Aborted because a dependency of this task has aborted (" + e.getTask().getId() + ")",
                e, taskToAbort, new LinkedList<>(), new LinkedList<>()
                )
              );
              }
          taskManager.setAborted(taskToAbort, e);
        }
      }
    }

    /**
     * Indicates that a {@link Task} is started to an external
     * {@link TaskExecutionHandler}
     * 
     * @param task
     *          Indicates the {@link Task} which has been started
     */
    private void notifyTaskStarted(final Task task) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskStarted(task);
      }
    }

    /**
     * Indicates that a {@link Task} is finished to an external
     * {@link TaskExecutionHandler}
     * 
     * @param task
     *          Indicates the {@link Task} which has been finished
     */
    private void notifyTaskFinished(final Task task) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskFinished(task);
      }
    }

    /**
     * Indicates that a {@link Task} is aborted to an external
     * {@link TaskExecutionHandler}
     * 
     * @param task
     *          Indicates the {@link Task} which has been aborted
     * 
     * @param e
     *          Indicates the {@link Exception} which causes the error
     */
    private void notifyTaskAborted(final Task task, final CompiTaskAbortedException e) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskAborted(task, e);
      }
    }

    private void notifyTaskIterationFinished(ForeachIteration iteration) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskIterationFinished(iteration);
      }
    }

    private void notifyTaskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskIterationAborted(iteration, e);
      }
    }

    private void notifyTaskIterationStarted(ForeachIteration iteration) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskIterationStarted(iteration);
      }
    }
  }

  private static class DummyProcess extends Process {

    @Override
    public OutputStream getOutputStream() {
      return null;
    }

    @Override
    public InputStream getInputStream() {
      return null;
    }

    @Override
    public InputStream getErrorStream() {
      return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
      return 0;
    }

    @Override
    public int exitValue() {
      return 0;
    }

    @Override
    public void destroy() {}
  }
}
