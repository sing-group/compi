package org.sing_group.compi.core;

import static org.sing_group.compi.core.loops.ForeachIteration.createIterationForForeach;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.runner.RunnersManager;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;
import org.xml.sax.SAXException;

/**
 * This class is the entry point for running Compi pipelines
 * 
 * Instances can be created by using {@link CompiRunConfiguration} objects, which include all parameters for the
 * execution.
 * 
 * It is possible to subscribe to task execution events by using 
 * {@link CompiApp#addTaskExecutionHandler(TaskExecutionHandler)}
 * 
 */
public class CompiApp {

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

    this.pipeline = config.getPipeline();

    if (config.getParamsFile() != null) {
      this.resolver = new XMLParamsFileVariableResolver(config.getParamsFile());
    } else {
      this.resolver = config.getResolver();
    }

    initializeTaskManager();

    initializeRunnersManager(config);

    disableTasksThatWillNotRun(config);

    initializeExecutorService(config.getMaxTasks());
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
          taskManager.taskStarted(taskToRun);
          if (taskToRun instanceof Foreach) {
            taskManager.initializeForEach((Foreach) taskToRun);
            loopCounterOfTask.put(
              taskToRun, new AtomicInteger(
                taskManager.getForEachTasks().get(taskToRun.getId()).size()
              )
            );
            if (taskManager.getForEachTasks().get(taskToRun.getId()).size() == 0) {
              // for loops without iterations, we need to add a dummy process,
              // because we expect the foreach task itself
              // is in taskLeft and we need to be notified that it has finished
              executorService.submit(
                new TaskRunnable(
                  createIterationForForeach((Foreach) taskToRun, null), this.executionHandler,
                  (task) -> {
                    return new DummyProcess();
                  }
                  )
                );
                } else {
              for (final ForeachIteration lp : taskManager.getForEachTasks().get(taskToRun.getId())) {
                executorService.submit(
                  new TaskRunnable(
                    lp, this.executionHandler, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()))
                  );
                  }
            }
          } else {
            executorService.submit(
              new TaskRunnable(
                taskToRun, this.executionHandler, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()))
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
    if (
      config.getSingleTask() != null && (config.getFromTasks() != null || config.getAfterTasks() != null
        || config.getUntilTask() != null || config.getBeforeTask() != null)
      ) {
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
  }

  private void initializeRunnersManager(CompiRunConfiguration config) throws IOException {
    if (config.getRunnersFile() != null) {
      this.runnersManager = new RunnersManager(config.getRunnersFile(), this.pipeline, this.resolver);
    } else {
      this.runnersManager = new RunnersManager(this.resolver);
    }
  }

  private void initializeTaskManager() {
    this.taskManager = new TaskManager(this.executionHandler, this.pipeline, this.resolver);
    this.taskManager.initializeDependencies();
  }

  private void disableTasksThatWillNotRun(CompiRunConfiguration config) {
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
  private void initializeExecutorService(final int threadNumber) throws IllegalArgumentException {
    if (threadNumber <= 0) {
      throw new IllegalArgumentException("The thread number must be higher than 0");
    } else {
      executorService = Executors.newFixedThreadPool(threadNumber);
    }
  }

  /**
   * Skips {@link Task}
   * 
   * @param task
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipTask(final String task) throws IllegalArgumentException {
    if (!taskManager.getTasksLeft().contains(task)) {
      throw new IllegalArgumentException("The task ID " + task + " doesn't exist");
    } else {
      taskManager.skipTask(task);
    }
  }

  /**
   * Skips {@link Task} until the {@link Task} where you want to start
   * 
   * @param advanceToTask
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipTasksBefore(final String advanceToTask) throws IllegalArgumentException {
    if (!taskManager.getTasksLeft().contains(advanceToTask)) {
      throw new IllegalArgumentException("The task ID " + advanceToTask + " doesn't exist");
    } else {
      taskManager.skipDependencies(advanceToTask);
    }
  }

  /**
   * Skips all {@link Task} but {@link Task}
   * 
   * @param singleTask
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipAllBut(String singleTask) {
    if (!taskManager.getTasksLeft().contains(singleTask)) {
      throw new IllegalArgumentException("The task ID " + singleTask + " doesn't exist");
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
  private void runUntil(String untilTask) {
    if (!taskManager.getTasksLeft().contains(untilTask)) {
      throw new IllegalArgumentException("The task ID " + untilTask + " doesn't exist");
    } else {
      taskManager.skipAllButDependencies(untilTask);
      taskManager.unSkipTask(untilTask);
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
  private void runBefore(String beforeTask) {
    if (!taskManager.getTasksLeft().contains(beforeTask)) {
      throw new IllegalArgumentException("The task ID " + beforeTask + " doesn't exist");
    } else {
      taskManager.skipAllButDependencies(beforeTask);
    }
  }

  private class CompiExecutionHandler implements TaskExecutionHandler {
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
        if (task instanceof ForeachIteration) {
          final Task parent = ((ForeachIteration) task).getParentForeachTask();
          if (loopCounterOfTask.get(parent).decrementAndGet() <= 0) {
            taskManager.taskFinished(parent);
            syncMonitor.notify();
          }
        } else {
          taskManager.taskFinished(task);
          syncMonitor.notify();
        }
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
    public void taskAborted(final Task task, final Exception e) {
      synchronized (syncMonitor) {
        notifyTaskAborted(task, e);
        taskManager.taskAborted(task, e);
        syncMonitor.notify();
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
    private void notifyTaskAborted(final Task task, final Exception e) {
      for (final TaskExecutionHandler handler : executionHandlers) {
        handler.taskAborted(task, e);
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