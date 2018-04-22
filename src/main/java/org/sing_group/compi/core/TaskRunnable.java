package org.sing_group.compi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.sing_group.compi.xmlio.entities.Task;

/**
 * Manage the {@link Task} execution.The {@link Task} starts and if there
 * is no error during the execution it will be marked as finished, if there is
 * an error during the execution it will be marked as aborted. If the
 * {@link Task} is skipped, it will be started and finished.
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

	/**
	 * 
	 * @param task
	 *            Indicates the {@link Task} to be executed
	 * @param executionHandler
	 *            Indicates the {@link TaskExecutionHandler} to manage the
	 *            {@link Task} execution
	 */
	public TaskRunnable(final Task task, final TaskExecutionHandler executionHandler) {
		this.task = task;
		this.executionHandler = executionHandler;
	}

	/**
	 * Execute the {@link Task} in a {@link Process}
	 */
	@Override
	public void run() {
		try {
			if (!this.task.isSkipped()) {
				String[] commandsToExecute = { "/bin/sh", "-c", this.task.getToExecute() };
				this.process = Runtime.getRuntime().exec(commandsToExecute);
				openLogBuffers(this.process);
				waitForProcess(this.process);
			} else {
				taskFinished(this.task);
			}
		} catch (IOException | InterruptedException e) {
			taskAborted(this.task, e);
		} finally {
			try {
				closeLogBuffers();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Checks the {@link Process} end
	 * 
	 * @param process
	 *            Indicates the {@link Process}
	 * @throws InterruptedException
	 *             If the {@link Process} ends with an error
	 */
	private void waitForProcess(final Process process) throws InterruptedException {
		if (process.waitFor() == 0) {
			taskFinished(this.task);
		} else {
			throw new InterruptedException();
		}
	}

	/**
	 * Opens the standard/error log buffers
	 * 
	 * @param process
	 *            Indicates {@link Process} to read its output
	 * @throws UnsupportedEncodingException
	 *             If the character encoding is not supported
	 * @throws FileNotFoundException
	 *             If the file can't be opened
	 */
	private void openLogBuffers(final Process process) throws UnsupportedEncodingException, FileNotFoundException {
		if (taskHasFileLog()) {
			out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(this.task.getFileLog(), true), "utf-8"));
			final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			startFileLog(stdOut);
		}

		if (taskHasFileErrorLog()) {
			err = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(this.task.getFileErrorLog(), true), "utf-8"));
			final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			startFileErrorLog(stdErr);
		}
	}

	/**
	 * Checks if the {@link Task} has a file error log
	 * 
	 * @return <code>true</code> if the {@link Task} has a file error log,
	 *         false otherwise
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
	 *             If an I/O exception of some sort has occurred
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
	 *            Indicates the {@link BufferedReader} where the output will be
	 *            read
	 * @throws UnsupportedEncodingException
	 *             If the character encoding is not supported
	 * @throws FileNotFoundException
	 *             If the file can't be opened
	 */
	private void startFileLog(final BufferedReader stdOut) throws UnsupportedEncodingException, FileNotFoundException {
		new Thread(() -> {
			String line;
			try {
				while ((line = stdOut.readLine()) != null) {
					out.write(line + System.getProperty("line.separator"));
				}
			} catch (final Exception e) {
			}
		}).start();
	}

	/**
	 * Creates a {@link Thread} to read the {@link Process} error output and
	 * write it in a {@link BufferedWriter}
	 * 
	 * @param stdOut
	 *            Indicates the {@link BufferedReader} where the output will be
	 *            read
	 * @throws UnsupportedEncodingException
	 *             If the character encoding is not supported
	 * @throws FileNotFoundException
	 *             If the file can't be opened
	 */
	private void startFileErrorLog(final BufferedReader stdErr)
			throws UnsupportedEncodingException, FileNotFoundException {
		new Thread(() -> {
			String line;
			try {
				while ((line = stdErr.readLine()) != null) {
					err.write(line + System.getProperty("line.separator"));
				}
			} catch (final Exception e) {
			}
		}).start();
	}

	/**
	 * Indicates which {@link Task} has been finished to the
	 * {@link TaskExecutionHandler}
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been finished
	 */
	private void taskFinished(final Task task) {
		if (task.isSkipped()) {
			executionHandler.taskFinished(task);
		} else {
			executionHandler.taskFinished(task);
		}
	}

	/**
	 * Indicates which {@link Task} has been aborted to the
	 * {@link TaskExecutionHandler}
	 * 
	 * @param task
	 *            Indicates the {@link Task} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception}
	 */
	private void taskAborted(final Task task, final Exception e) {
		executionHandler.taskAborted(task, e);
	}

}
