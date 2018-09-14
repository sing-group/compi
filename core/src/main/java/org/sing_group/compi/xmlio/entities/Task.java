package org.sing_group.compi.xmlio.entities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

/**
 * Represents a {@link Task} obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
@XmlRootElement(name = "task")
public class Task implements Cloneable {

	private String id;
	private String after;
	private String exec;
	private String interpreter;
	private List<String> parameters = new LinkedList<>();
	private String parametersString;
	private String toExecute;
	private String fileLog;
	private String fileErrorLog;
	private boolean isRunning = false;
	private boolean isFinished = false;
	private boolean isAborted = false;
	private boolean isSkipped = false;

	/**
	 * Getter of the id attribute
	 * 
	 * @return The value of the id attribute
	 */
	@XmlAttribute
	public String getId() {
		return id;
	}

	/**
	 * Changes the value of the id attribute
	 * 
	 * @param id
	 *            attribute
	 */
	public void setId(final String id) {
		this.id = id.replaceAll(" ", "");
	}

	/**
	 * Getter of the after attribute
	 * 
	 * @return The value of the after attribute
	 */
	@XmlAttribute
	public String getAfter() {
		return after;
	}
	
	/**
   * Getter of the interpreter attribute
   * 
   * @return The value of the interpreter attribute
   */
	@XmlAttribute
	public String getInterpreter() {
    return interpreter;
  }

	 /**
   * Changes the value of the interpreter attribute
   * 
   * @param interpreter
   *            attribute
   */
	public void setInterpreter(String interpreter) {
    this.interpreter = interpreter;
  }
	
	/**
	 * Changes the value of the after attribute
	 * 
	 * @param after attribute
	 */
	public void setAfter(final String after) {
		this.after = after.replaceAll(" ", "");
	}
	
	public void setParametersString(String parametersString) {
    this.parametersString = parametersString;
  }
	
	@XmlAttribute(name="params")
	public String getParametersString() {
    return parametersString;
  }
	/**
	 * Getter of the fileLog attribute
	 * 
	 * @return The value of the fileLog attribute
	 */
	@XmlAttribute
	public String getFileLog() {
		return fileLog;
	}

	/**
	 * Changes the value of the fileLog attribute
	 * 
	 * @param fileLog
	 *            attribute
	 */
	public void setFileLog(final String fileLog) {
		this.fileLog = fileLog;
	}

	/**
	 * Getter of the fileErrorLog attribute
	 * 
	 * @return The value of the fileErrorLog attribute
	 */
	@XmlAttribute
	public String getFileErrorLog() {
		return fileErrorLog;
	}

	/**
	 * Changes the value of the fileErrorLog attribute
	 * 
	 * @param fileErrorLog
	 *            attribute
	 */
	public void setFileErrorLog(final String fileErrorLog) {
		this.fileErrorLog = fileErrorLog;
	}


	/**
	 * Getter of the exec attribute
	 * 
	 * @return The value of the exec attribute
	 */
	@XmlValue
	public String getExec() {
		return exec;
	}

	/**
	 * Changes the value of the exec attribute
	 * 
	 * @param exec
	 *            attribute
	 */
	public void setExec(final String exec) {
		this.exec = exec.trim();
		this.toExecute = this.exec;
	}

	/**
	 * Getter of the isRunning attribute
	 * 
	 * @return The value of the isRunning attribute
	 */
	@XmlTransient
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Changes the value of the isRunning attribute
	 * 
	 * @param isRunning
	 *            attribute
	 */
	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * Getter of the isFinished attribute
	 * 
	 * @return The value of the isFinished attribute
	 */
	@XmlTransient
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Changes the value of the isFinished attribute
	 * 
	 * @param isFinished
	 *            attribute
	 */
	public void setFinished(final boolean isFinished) {
		this.isFinished = isFinished;
	}

	/**
	 * Getter of the parameters attribute
	 * 
	 * @return The value of the paramters attribute
	 */
	@XmlTransient
	public List<String> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	/**
	 * Adds a parameter to the list of parameters
	 * 
	 * @param parameter to add to the list
	 */
	public void addParameter(final String parameter) {
		this.parameters.add(parameter);
	}

	/**
	 * Getter of the toExecute attribute
	 * 
	 * @return The value of the toExecute attribute
	 */
	@XmlTransient
	public String getToExecute() {
		return toExecute;
	}

	/**
	 * Changes the value of the toExecute attribute
	 * 
	 * @param toExecute
	 *            attribute
	 */
	public void setToExecute(final String toExecute) {
		this.toExecute = toExecute;
	}

	/**
	 * Getter of the isAborted attribute
	 * 
	 * @return The value of the isAborted attribute
	 */
	@XmlTransient
	public boolean isAborted() {
		return isAborted;
	}

	/**
	 * Changes the value of the isAborted attribute
	 * 
	 * @param isAborted
	 *            attribute
	 */
	public void setAborted(final boolean isAborted) {
		this.isAborted = isAborted;
	}

	/**
	 * Getter of the isSkipped attribute
	 * 
	 * @return The value of the isSkipped attribute
	 */
	@XmlTransient
	public boolean isSkipped() {
		return isSkipped;
	}

	/**
	 * Changes the value of the isSkipped attribute
	 * 
	 * @param isSkipped
	 *            attribute
	 */
	public void setSkipped(final boolean isSkipped) {
		this.isSkipped = isSkipped;
	}

	/**
	 * Creates a clone of a {@link Task}
	 */
	@Override
	public Task clone() {
		try {
			return (Task) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}