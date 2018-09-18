package org.sing_group.compi.core;

import java.io.File;
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

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.runner.RunnersManager;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;
import org.sing_group.compi.xmlio.PipelineParserFactory;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;
import org.xml.sax.SAXException;

/**
 * Executes all the {@link Task} contained in the {@link Pipeline}
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class CompiApp implements TaskExecutionHandler {

  private Pipeline pipeline;
  private TaskManager taskManager;
  private VariableResolver resolver;
  private ExecutorService executorService;
  final File pipelineFile;
  private final Map<Task, Task> parentTask = new HashMap<>();
  private final Map<Task, AtomicInteger> loopCount = new HashMap<>();
  private final List<TaskExecutionHandler> executionHandlers = new ArrayList<>();
  private RunnersManager runnersManager;

  /**
   * Creates a Compi application for running a pipeline. The configuration of the execution is provided in a
   * {@link CompiRunConfiguration} object.
   * 
   * @param config The configuration of the execution
   * @param errors An object to store pipeline validation errors
   * 
   * @throws PipelineValidationException If the pipeline cannot be executed due to a bad pipeline file
   * @throws IllegalArgumentException If some of the configuration parameters are wrong
   * @throws IOException If there is a problem accessing pipeline files, parameter files, etc.
   */
  public CompiApp(
    CompiRunConfiguration config,
    List<ValidationError> errors
  ) throws PipelineValidationException, IllegalArgumentException, IOException {

    if (config.getSingleTask() != null && (config.getFromTasks() != null || config.getAfterTasks() != null || config.getUntilTask() != null || config.getBeforeTask() != null)) {
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

    this.pipelineFile = config.getPipelineFile();

    PipelineValidator validator = new PipelineValidator(this.pipelineFile);
    List<ValidationError> _errors = validator.validate();

    if (errors != null) {
      errors.addAll(_errors);
    }

    if (_errors.stream().filter(error -> error.getType().isError()).count() > 0) {
      System.err.println(_errors);
      throw new PipelineValidationException(_errors);
    }

    this.pipeline = PipelineParserFactory.createPipelineParser().parsePipeline(this.pipelineFile);
    
    if (config.getParamsFile() != null) {
      this.resolver = new XMLParamsFileVariableResolver(config.getParamsFile());
    } else {
      this.resolver = config.getResolver();
    }

    this.taskManager = new TaskManager(this, this.pipeline, this.resolver);

    if (config.getRunnersFile() != null) {
      this.runnersManager = new RunnersManager(config.getRunnersFile(), this.pipeline, this.resolver);
    } else {
      this.runnersManager = new RunnersManager(this.resolver);
    }
    
    initializePipeline();

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

    initializeExecutorService(config.getMaxTasks());
  }

  public CompiApp(CompiRunConfiguration config
  ) throws JAXBException, PipelineValidationException, IllegalArgumentException, IOException {
    this(config, null);
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

    synchronized (this) {
      while (!taskManager.getTasksLeft().isEmpty()) {
        for (final Task taskToRun : taskManager.getRunnableTasks()) {
          this.getTaskManager().taskStarted(taskToRun);
          if (taskHasForEach(taskToRun)) {
            taskManager.initializeForEach((Foreach) taskToRun);
            loopCount.put(
              taskToRun, new AtomicInteger(
                taskManager.getForEachTasks().get(taskToRun.getId()).size()
              )
            );
            if (taskManager.getForEachTasks().get(taskToRun.getId()).size() == 0) {
              // for loops without iterations, we need to add a dummy process,
              // because we expect the foreach task itself
              // is in taskLeft and we need to be notified that it has finished
              this.parentTask.put(taskToRun, taskToRun);
              executorService.submit(
                new TaskRunnable(
                  taskToRun, this,
                  (task) -> {
                    return new DummyProcess();
                  }
                  )
                );
                } else {
              for (final ForeachIteration lp : taskManager.getForEachTasks().get(taskToRun.getId())) {
                final Task cloned = taskToRun.clone();
                ((Foreach) cloned).setForeachIteration(lp);
               /* if (!cloned.isSkipped()) {
                  resolveTask(cloned);
                }*/
                parentTask.put(cloned, taskToRun);
                executorService.submit(
                  new TaskRunnable(cloned, this, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()))
                  );
                  }
            }
          } else {
            /*if (!taskToRun.isSkipped())
              resolveTask(taskToRun);*/

            executorService.submit(
              new TaskRunnable(taskToRun, this, this.runnersManager.getProcessCreatorForTask(taskToRun.getId()))
              );
              }
        }
        this.wait();
      }
    }

    executorService.shutdown();

    // wait for remaining tasks that are currently running
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
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
   * Checks if the {@link Task} has the foreach tag
   * 
   * @param taskToRun
   *          the {@link Task} to check
   * @return <code>true</code> if the {@link Task} has a foreach tag,
   *         <code>false</code> otherwise
   */
  private boolean taskHasForEach(final Task taskToRun) {
    return taskToRun instanceof Foreach;
  }

  /**
   * Initializes all the parameters to allow the {@link Task} execution
   */
  private void initializePipeline() {
    taskManager.checkAfterIds();
    // PipelineParser.solveExec(pipeline.getTasks());
    taskManager.initializeDependencies();
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
      this.getTaskManager().skipTask(task);
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
      this.getTaskManager().skipDependencies(advanceToTask);
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
      this.getTaskManager().skipAllTasksBut(singleTask);
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
      this.getTaskManager().skipAllButDependencies(untilTask);
      this.getTaskManager().unSkipTask(untilTask);
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
      this.getTaskManager().skipAllButDependencies(beforeTask);
    }
  }

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
   * Indicates that a {@link Task} is started to an external
   * {@link TaskExecutionHandler}
   * 
   * @param task
   *          Indicates the {@link Task} which has been started
   */
  private void notifyTaskStarted(final Task task) {
    for (final TaskExecutionHandler handler : this.executionHandlers) {
      handler.taskStarted(task);
    }
  }

  /**
   * Indicates that a {@link Task} is finished and notifies
   * 
   * @param task
   *          Indicates the {@link Task} which has been started
   */
  @Override
  synchronized public void taskFinished(final Task task) {
    if (taskHasForEach(task)) {
      final Task parent = this.parentTask.get(task);
      if (loopCount.get(parent).decrementAndGet() <= 0) {
        this.getTaskManager().taskFinished(parent);
        this.notify();
      }
    } else {
      this.getTaskManager().taskFinished(task);
      this.notify();
    }
    if (!task.isSkipped()) {
      this.notifyTaskFinished(task);
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
    for (final TaskExecutionHandler handler : this.executionHandlers) {
      handler.taskFinished(task);
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
  synchronized public void taskAborted(final Task task, final Exception e) {
    this.notifyTaskAborted(task, e);
    this.getTaskManager().taskAborted(task, e);
    this.notify();
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
    for (final TaskExecutionHandler handler : this.executionHandlers) {
      handler.taskAborted(task, e);
    }
  }

  /**
   * Getter of the taskManager attribute
   * 
   * @return The value of the taskManager attribute
   */
  public TaskManager getTaskManager() {
    return taskManager;
  }

  /**
   * Getter of the parentTask attribute
   * 
   * @return The value of the parentTask attribute
   */
  public Map<Task, Task> getParentTask() {
    return parentTask;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setResolver(VariableResolver resolver) {
    this.resolver = resolver;
    this.taskManager.setResolver(resolver);
    this.runnersManager.setResolver(resolver);
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