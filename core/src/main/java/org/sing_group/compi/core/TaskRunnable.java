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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.runner.ProcessCreator;

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
  private File stdOutLog;
  private File stdErrorLog;
  private boolean overwriteLog;
  private final int MAX_OUTPUT_MESSAGES = 100;
  private final LinkedList<String> stdOutLastMessages = new LinkedList<>();
  private final LinkedList<String> stdErrLastMessages = new LinkedList<>();
  private Thread stdErrThread;
  private Thread stdOutThread;
  private boolean showStdOuts;

  /**
   * 
   * @param task
   *          Indicates the {@link Task} to be executed
   * @param executionHandler
   *          Indicates the {@link TaskExecutionHandler} to manage the
   *          {@link Task} execution
   * @param processCreator
   *          Indicates the {@link ProcessCreator} to create the native process 
   * @param stdOutLog
   *          Indicates the file to redirect the task process's stdout
   * @param stdErrorLog
   *          Indicates the file to redirect the task process's stderr
   * @param overwriteLog
   *          Indicates if logs should be overwritten, instead of appended
   * @param showStdOuts
   *          Indicates if the process standard error and outs should be forwarded to System.out and System.err
   */
  public TaskRunnable(
    final Task task, final TaskExecutionHandler executionHandler, ProcessCreator processCreator, File stdOutLog,
    File stdErrorLog, boolean overwriteLog, boolean showStdOuts
  ) {
    this.task = task;
    this.executionHandler = executionHandler;
    this.processCreator = processCreator;
    this.stdOutLog = stdOutLog;
    this.stdErrorLog = stdErrorLog;
    this.overwriteLog = overwriteLog;
    this.showStdOuts = showStdOuts;
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

          if (
            this.task instanceof ForeachIteration &&
              ((ForeachIteration) task).getParentForeachTask().isAborted()
          ) {
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
      } catch (CompiTaskAbortedException e) {
        taskAborted(this.task, e);
      } catch (IOException e) {
        taskAborted(
          this.task,
          new CompiTaskAbortedException(
            "An I/O error ocurred: " + e.getMessage(), e, this.task, this.stdOutLastMessages, this.stdErrLastMessages
          )
        );
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
  private void waitForProcess(final Process process) throws CompiTaskAbortedException {
    int exitStatus = 0;
    try {
      exitStatus = process.waitFor();
      
      if (stdErrThread != null) stdErrThread.join();
      if (stdOutThread != null) stdOutThread.join();
      
      if ((exitStatus) != 0) {
        throw new CompiTaskAbortedException(
          "The process has exited with a non-zero status: " + exitStatus, null, this.task, this.stdOutLastMessages,
          this.stdErrLastMessages
        );
      }
    } catch (InterruptedException e) {
      throw new CompiTaskAbortedException(
        "The process has been interrupted", e, this.task, this.stdOutLastMessages,
        this.stdErrLastMessages
      );
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
          new OutputStreamWriter(new FileOutputStream(this.stdOutLog, !this.overwriteLog), "utf-8")
        );
      task.setStdOutLogFile(this.stdOutLog);
    }
    final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
    this.stdOutThread = startLog(stdOut, this.out, this.stdOutLastMessages, System.out);

    if (taskHasFileErrorLog()) {
      err =
        new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(this.stdErrorLog, !this.overwriteLog), "utf-8")
        );
      task.setStdErrLogFile(this.stdErrorLog);
    }
    
    final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    this.stdErrThread = startLog(stdErr, this.err, this.stdErrLastMessages, System.err);
  }

  /**
   * Checks if the {@link Task} has a file error log
   * 
   * @return <code>true</code> if the {@link Task} has a file error log, false
   *         otherwise
   */
  private boolean taskHasFileErrorLog() {
    return this.stdErrorLog != null;
  }

  /**
   * Checks if the {@link Task} has a file log
   * 
   * @return <code>true</code> if the {@link Task} has a file log, false
   *         otherwise
   */
  private boolean taskHasFileLog() {
    return this.stdOutLog != null;
  }

  /**
   * Closes the {@link BufferedWriter} used to read the standard/error output
   * 
   * @throws IOException
   *           If an I/O exception of some sort has occurred
   */
  private void closeLogBuffers() throws IOException {
    try {
      if (stdErrThread != null) stdErrThread.join();
      if (stdOutThread != null) stdOutThread.join();
    } catch (InterruptedException e) {e.printStackTrace();}
    
    if (taskHasFileLog() && !this.task.isSkipped()) {
      out.flush();
      out.close();
    }
    if (taskHasFileErrorLog() && !this.task.isSkipped()) {
      err.flush();
      err.close();
    }
  }

  private Thread startLog(final BufferedReader in, final BufferedWriter out, final LinkedList<String> lastMessages, PrintStream stdOut)
    throws UnsupportedEncodingException, FileNotFoundException {
    if (out != null) {
      try {
        out.write("\n-- Starting log on " + new Date() + " --\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    Thread logThread = new Thread(() -> {
      String line;
      try {
        
        while ((line = in.readLine()) != null) {
          if (showStdOuts) {
            stdOut.println(line);
          }
          if (out != null) {
            out.write(line + System.getProperty("line.separator"));
          }
          if (lastMessages.size() == MAX_OUTPUT_MESSAGES) {
            lastMessages.poll();
          }
          lastMessages.offer(line);
        }
      } catch (final Exception e) {}
    });
    logThread.start();
    return logThread;
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
  private void taskAborted(final Task task, final CompiTaskAbortedException e) {
    if (task instanceof Foreach) {
      executionHandler.taskIterationAborted((ForeachIteration) task, e);
    } else {
      executionHandler.taskAborted(task, e);
    }
  }
}
