package org.sing_group.compi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.runner.ProcessCreator;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;

/**
 * Manage the {@link Task} execution.The {@link Task} starts and if there is no
 * error during the execution it will be marked as finished, if there is an
 * error during the execution it will be marked as aborted. If the {@link Task}
 * is skipped, it will be started and finished.
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class TaskRunnable implements Runnable {
  private final Task task;
  private final TaskExecutionHandler executionHandler;
  private BufferedWriter out;
  private BufferedWriter err;
  private Process process;
  private ProcessCreator processCreator;

  /**
   * 
   * @param task
   *          Indicates the {@link Task} to be executed
   * @param executionHandler
   *          Indicates the {@link TaskExecutionHandler} to manage the
   *          {@link Task} execution
   * @param processCreator
   *          an interface to create a process for running the task
   */
  public TaskRunnable(final Task task, final TaskExecutionHandler executionHandler, ProcessCreator processCreator) {
    this.task = task;
    this.executionHandler = executionHandler;
    this.processCreator = processCreator;
  }

  /**
   * Execute the {@link Task} in a {@link Process}
   */
  @Override
  public void run() {
    try {
      try {
        if (!this.task.isSkipped()) {
          taskStarted(this.task);

          if (this.task instanceof ForeachIteration &&
        		  ((ForeachIteration) task).getParentForeachTask().isAborted()) {
        	  taskAborted(this.task, null);
          } else {
        	  this.process = this.getProcess(this.task);
        	  openLogBuffers(this.process);
        	  waitForProcess(this.process);
        	  taskFinished(this.task);
          }
        } else {
          taskFinished(this.task);
        }
      } catch (IOException | InterruptedException e) {
        taskAborted(this.task, e);
      } finally {
        try {
          closeLogBuffers();
        } catch (IOException e) {}
      }
    } catch (Throwable e) {
      e.printStackTrace();
      throw e;
    }
  }

  private Process getProcess(Task task) {
    return this.processCreator.createProcess(task);
  }

  /**
   * Checks the {@link Process} end
   * 
   * @param process
   *          Indicates the {@link Process}
   * @throws InterruptedException
   *           If the {@link Process} ends with an error
   */
  private void waitForProcess(final Process process) throws InterruptedException {
    if (process.waitFor() == 0) {

      return;
    } else {
      throw new InterruptedException();
    }
  }

  /**
   * Opens the standard/error log buffers
   * 
   * @param process
   *          Indicates {@link Process} to read its output
   * @throws UnsupportedEncodingException
   *           If the character encoding is not supported
   * @throws FileNotFoundException
   *           If the file can't be opened
   */
  private void openLogBuffers(final Process process) throws UnsupportedEncodingException, FileNotFoundException {
    if (taskHasFileLog()) {
      out =
        new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(this.task.getFileLog(), true), "utf-8")
        );
      final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
      startFileLog(stdOut);
    }

    if (taskHasFileErrorLog()) {
      err =
        new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(this.task.getFileErrorLog(), true), "utf-8")
        );
      final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      startFileErrorLog(stdErr);
    }
  }

  /**
   * Checks if the {@link Task} has a file error log
   * 
   * @return <code>true</code> if the {@link Task} has a file error log, false
   *         otherwise
   */
  private boolean taskHasFileErrorLog() {
    return this.task.getFileErrorLog() != null;
  }

  /**
   * Checks if the {@link Task} has a file log
   * 
   * @return <code>true</code> if the {@link Task} has a file log, false
   *         otherwise
   */
  private boolean taskHasFileLog() {
    return this.task.getFileLog() != null;
  }

  /**
   * Closes the {@link BufferedWriter} used to read the standard/error output
   * 
   * @throws IOException
   *           If an I/O exception of some sort has occurred
   */
  private void closeLogBuffers() throws IOException {
    if (taskHasFileLog()) {
      out.flush();
      out.close();
    }
    if (taskHasFileErrorLog()) {
      err.flush();
      err.close();
    }
  }

  /**
   * Creates a {@link Thread} to read the {@link Process} standard output and
   * write it in a {@link BufferedWriter}
   * 
   * @param stdOut
   *          Indicates the {@link BufferedReader} where the output will be read
   * @throws UnsupportedEncodingException
   *           If the character encoding is not supported
   * @throws FileNotFoundException
   *           If the file can't be opened
   */
  private void startFileLog(final BufferedReader stdOut) throws UnsupportedEncodingException, FileNotFoundException {
    new Thread(() -> {
      String line;
      try {
        while ((line = stdOut.readLine()) != null) {
          out.write(line + System.getProperty("line.separator"));
        }
      } catch (final Exception e) {}
    }).start();
  }

  /**
   * Creates a {@link Thread} to read the {@link Process} error output and write
   * it in a {@link BufferedWriter}
   * 
   * @param stdOut
   *          Indicates the {@link BufferedReader} where the output will be read
   * @throws UnsupportedEncodingException
   *           If the character encoding is not supported
   * @throws FileNotFoundException
   *           If the file can't be opened
   */
  private void startFileErrorLog(final BufferedReader stdErr)
    throws UnsupportedEncodingException, FileNotFoundException {
    new Thread(() -> {
      String line;
      try {
        while ((line = stdErr.readLine()) != null) {
          err.write(line + System.getProperty("line.separator"));
        }
      } catch (final Exception e) {}
    }).start();
  }

  /**
   * Notifies that the task been finished
   * 
   * @param task
   *          the task
   */
  private void taskFinished(final Task task) {
    if (task instanceof Foreach) {
      executionHandler.taskIterationFinished((ForeachIteration) task);
    } else {
      executionHandler.taskFinished(task);
    }
  }

  /**
   * Notifies that the task has been started
   * 
   * @param task
   *          the task
   */
  private void taskStarted(Task task) {
    if (task instanceof Foreach) {
      executionHandler.taskIterationStarted((ForeachIteration) task);
    } else {
      executionHandler.taskStarted(task);
    }
  }

  /**
   * Notifies that the task has been aborted
   * 
   * @param task
   *          the task
   * @param e
   *          the error causing the abortion
   */
  private void taskAborted(final Task task, final Exception e) {
    if (task instanceof Foreach) {
      executionHandler.taskIterationAborted((ForeachIteration) task, e);
    } else {
      executionHandler.taskAborted(task, e);
    }
  }
}
