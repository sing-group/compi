package es.uvigo.esei.compi.core;

import es.uvigo.esei.compi.xmlio.entities.Program;

/**
 * Indicates when a {@link Program} is started/finished/aborted
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public interface ProgramExecutionHandler {
	/**
	 * Indicates when a {@link Program} has been started
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	void programStarted(Program program);

	/**
	 * Indicates when a {@link Program} has been finished
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been finished
	 */
	void programFinished(Program program);

	/**
	 * Indicates when a {@link Program} has been aborted
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	void programAborted(Program program, Exception e);
}
