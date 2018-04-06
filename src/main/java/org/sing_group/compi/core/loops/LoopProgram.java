package org.sing_group.compi.core.loops;

/**
 * Auxiliary class to obtain the program command to execute
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class LoopProgram {

	private String exec;
	private String toExecute;
	private String source;
	private String as;

	public LoopProgram() {
	}

	/**
	 * 
	 * @param exec
	 *            Contains the value of the program exec tag
	 * @param source
	 *            Contains the value of the program source tag
	 * @param as
	 *            Contains the value of the program as tag
	 */
	public LoopProgram(final String exec, final String source, final String as) {
		this.exec = exec;
		this.toExecute = exec;
		this.as = as;
		this.source = source;
	}

	/**
	 * Getter method of the exec attribute
	 * 
	 * @return The value of the exec attribute
	 */
	public String getExec() {
		return exec;
	}

	/**
	 * Getter method of the toExecute attribute
	 * 
	 * @return The value of the toExecute attribute
	 */
	public String getToExecute() {
		return toExecute;
	}

	/**
	 * Changes the value of the toExecute attribute
	 * 
	 * @param toExecute
	 *            Global variable
	 */
	public void setToExecute(final String toExecute) {
		this.toExecute = toExecute;
	}

	/**
	 * Getter method of the as attribute
	 * 
	 * @return The value of the as attribute
	 */
	public String getAs() {
		return as;
	}

	/**
	 * Getter method of the source attribute
	 * 
	 * @return The value of the source attribute
	 */
	public String getSource() {
		return source;
	}

}
