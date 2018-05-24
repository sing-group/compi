package org.sing_group.compi.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.loops.LoopTask;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;
import org.sing_group.compi.xmlio.PipelineParser;
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
  String paramsFile;
  String threadNumber;
  String advanceToTask;
  private final Map<Task, Task> parentTask = new HashMap<>();
  private final Map<Task, AtomicInteger> loopCount = new HashMap<>();
  private final List<TaskExecutionHandler> executionHandlers = new ArrayList<>();

  /**
   * Constructs the CompiApp
   * 
   * @param pipelineFile
   *          the pipeline file
   * @param threadNumber
   *          the size of the thread pool
   * @param resolver
   *          an object to resolve variables
   * @param advanceToTask
   *          the task to start the pipeline from, all previous dependencies are
   *          skipped
   * @param singleTask
   *          run a single pipeline task
   * @param errors
   *          a list that will be filled with validation errors and warnings (if
   *          any)
   * @throws JAXBException
   *           If there is an error in the XML unmarshal process
   * @throws PipelineValidationException
   *           If the validation process gives an error (not only warnings)
   */

  public CompiApp(
    final String pipelineFile, final int threadNumber, final VariableResolver resolver, final String advanceToTask,
    final String singleTask, List<ValidationError> errors
  ) throws JAXBException, PipelineValidationException {
    this.pipelineFile = new File(pipelineFile);

    PipelineValidator validator = new PipelineValidator(this.pipelineFile);
    List<ValidationError> _errors = validator.validate();

    if (errors != null) {
      errors.addAll(_errors);
    }

    if (_errors.stream().filter(error -> error.getType().isError()).count() > 0) {
      throw new PipelineValidationException(_errors);
    }

    this.pipeline = PipelineParser.parsePipeline(this.pipelineFile);
    this.resolver = resolver;

    this.taskManager = new TaskManager(this, this.pipeline, this.resolver);
    initializePipeline(advanceToTask, singleTask);
    initializeExecutorService(threadNumber);
  }

  public CompiApp(
    final String pipelineFile, final int threadNumber, final VariableResolver resolver, final String advanceToTask,
    final String singleTask
  ) throws JAXBException, PipelineValidationException {
    this(pipelineFile, threadNumber, resolver, advanceToTask, singleTask, null);
  }
  
  public CompiApp(
    final String pipelineFile, final int threadNumber, String paramsFile, final String advanceToTask,
    final String singleTask, final List<ValidationError> errors
  ) throws JAXBException, PipelineValidationException {
    this(pipelineFile, threadNumber, new XMLParamsFileVariableResolver(paramsFile), advanceToTask, singleTask, errors);
  }
  
  public CompiApp(
    final String pipelineFile, final int threadNumber, String paramsFile, final String advanceToTask,
    final String singleTask
  ) throws JAXBException, PipelineValidationException {
    this(pipelineFile, threadNumber, paramsFile, advanceToTask, singleTask, null);
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
            if (!taskToRun.isSkipped())
              resolveTask(taskToRun);
            loopCount.put(
              taskToRun, new AtomicInteger(
                taskManager.getForEachTasks().get(taskToRun.getId()).size()
              )
            );
            for (final LoopTask lp : taskManager.getForEachTasks().get(taskToRun.getId())) {
              final Task cloned = taskToRun.clone();
              parentTask.put(cloned, taskToRun);
              cloned.setToExecute(lp.getToExecute());
              executorService.submit(new TaskRunnable(cloned, this));
            }
          } else {
            if (!taskToRun.isSkipped())
              resolveTask(taskToRun);

            executorService.submit(new TaskRunnable(taskToRun, this));
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
   * 
   * @param advanceToTask
   *          Indicates the {@link Task} ID which you want to advance
   */
  private void initializePipeline(
    final String advanceToTask,
    final String singleTask
  ) {
    if (advanceToTask != null && singleTask != null) {
      throw new IllegalArgumentException("advanceToTask or singleTask must be null");
    }

    taskManager.checkAfterIds();
    // PipelineParser.solveExec(pipeline.getTasks());
    taskManager.initializeDependencies();

    if (advanceToTask != null) {
      skipTasks(advanceToTask);
    }
    if (singleTask != null) {
      skipAllBut(singleTask);
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
   * Skips {@link Task} until the {@link Task} where you want to start
   * 
   * @param advanceToTask
   *          Indicates the {@link Task} ID
   * @throws IllegalArgumentException
   *           If the {@link Task} ID doesn't exist
   */
  private void skipTasks(final String advanceToTask) throws IllegalArgumentException {
    if (!taskManager.getTasksLeft().contains(advanceToTask)) {
      throw new IllegalArgumentException("The task ID " + advanceToTask + " doesn't exist");
    } else {
      this.getTaskManager().skipTasks(advanceToTask);
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
   * Resolves the command to execute of a given task
   *
   * @param t
   *          The task to resolve
   * 
   * @throws IllegalArgumentException
   *           If the {@link Task} attribute "as" isn't contained in the exec
   *           tag
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private void resolveTask(Task t)
    throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {

    if (taskHasForEach(t)) {
      if (t.getParameters().contains(((Foreach) t).getAs())) {
        for (final LoopTask lp : taskManager.getForEachTasks().get(t.getId())) {
          for (final String tag : t.getParameters()) {
            if (lp.getAs().equals(tag)) {
              lp.setToExecute(lp.getToExecute().replace("${" + tag + "}", lp.getSource()));
            } else {
              final String parsed = resolver.resolveVariable(tag);
              lp.setToExecute(lp.getToExecute().replace("${" + tag + "}", parsed));
            }
          }
        }
      } else {
        throw new IllegalArgumentException(
          "The as attribute of the task " + t.getId() + " ins't contained in the exec tag"
        );
      }
    } else {
      for (final String parameter : t.getParameters()) {
        final String variableValue = resolver.resolveVariable(parameter);
        t.setToExecute(t.getToExecute().replace("${" + parameter + "}", variableValue));
      }
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
      if (loopCount.get(parent).decrementAndGet() == 0) {
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
  }

}