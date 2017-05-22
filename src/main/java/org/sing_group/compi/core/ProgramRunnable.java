package org.sing_group.compi.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.sing_group.compi.xmlio.entities.Program;

/**
 * Manage the {@link Program} execution.The {@link Program} starts and if there
 * is no error during the execution it will be marked as finished, if there is
 * an error during the execution it will be marked as aborted. If the
 * {@link Program} is skipped, it will be started and finished.
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ProgramRunnable implements Runnable {
	private final Program program;
	private final ProgramExecutionHandler executionHandler;
	private BufferedWriter out;
	private BufferedWriter err;
	private Process process;

	/**
	 * 
	 * @param program
	 *            Indicates the {@link Program} to be executed
	 * @param executionHandler
	 *            Indicates the {@link ProgramExecutionHandler} to manage the
	 *            {@link Program} execution
	 */
	public ProgramRunnable(final Program program, final ProgramExecutionHandler executionHandler) {
		this.program = program;
		this.executionHandler = executionHandler;
	}

	/**
	 * Execute the {@link Program} in a {@link Process}
	 */
	@Override
	public void run() {
		try {
			if (!this.program.isSkipped()) {
				String[] commandsToExecute = { "/bin/sh", "-c", this.program.getToExecute() };
				this.process = Runtime.getRuntime().exec(commandsToExecute);
				openLogBuffers(this.process);
				waitForProcess(this.process);
			} else {
				programFinished(this.program);
			}
		} catch (IOException | InterruptedException e) {
			programAborted(this.program, e);
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
			programFinished(this.program);
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
		if (programsHasFileLog()) {
			out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(this.program.getFileLog(), true), "utf-8"));
			final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			startFileLog(stdOut);
		}

		if (programHasFileErrorLog()) {
			err = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(this.program.getFileErrorLog(), true), "utf-8"));
			final BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			startFileErrorLog(stdErr);
		}
	}

	/**
	 * Checks if the {@link Program} has a file error log
	 * 
	 * @return <code>true</code> if the {@link Program} has a file error log,
	 *         false otherwise
	 */
	private boolean programHasFileErrorLog() {
		return this.program.getFileErrorLog() != null;
	}

	/**
	 * Checks if the {@link Program} has a file log
	 * 
	 * @return <code>true</code> if the {@link Program} has a file log, false
	 *         otherwise
	 */
	private boolean programsHasFileLog() {
		return this.program.getFileLog() != null;
	}

	/**
	 * Closes the {@link BufferedWriter} used to read the standard/error output
	 * 
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 */
	private void closeLogBuffers() throws IOException {
		if (programsHasFileLog()) {
			out.flush();
			out.close();
		}
		if (programHasFileErrorLog()) {
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
	 * Indicates which {@link Program} has been finished to the
	 * {@link ProgramExecutionHandler}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been finished
	 */
	private void programFinished(final Program program) {
		if (program.isSkipped()) {
			executionHandler.programFinished(program);
		} else {
			executionHandler.programFinished(program);
		}
	}

	/**
	 * Indicates which {@link Program} has been aborted to the
	 * {@link ProgramExecutionHandler}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception}
	 */
	private void programAborted(final Program program, final Exception e) {
		executionHandler.programAborted(program, e);
	}

}
