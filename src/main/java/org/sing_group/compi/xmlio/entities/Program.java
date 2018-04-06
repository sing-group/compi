package org.sing_group.compi.xmlio.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a {@link Program} obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
@XmlRootElement(name = "program")
public class Program implements Cloneable {

	private String id;
	private String dependsOn;
	private Foreach foreach;
	private String exec;
	private List<String> execStrings = new LinkedList<>();
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
	 * Getter of the dependsOn attribute
	 * 
	 * @return The value of the dependsOn attribute
	 */
	@XmlAttribute
	public String getDependsOn() {
		return dependsOn;
	}

	/**
	 * Changes the value of the dependsOn attribute
	 * 
	 * @param dependsOn
	 *            attribute
	 */
	public void setDependsOn(final String dependsOn) {
		this.dependsOn = dependsOn.replaceAll(" ", "");
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
	 * Getter of the foreach attribute
	 * 
	 * @return The value of the foreach attribute
	 */
	@XmlElement
	public Foreach getForeach() {
		return foreach;
	}

	/**
	 * Changes the value of the foreach attribute
	 * 
	 * @param foreach
	 *            attribute
	 */
	public void setForeach(final Foreach foreach) {
		this.foreach = foreach;
	}

	/**
	 * Getter of the exec attribute
	 * 
	 * @return The value of the exec attribute
	 */
	@XmlElement
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
	 * Getter of the execStrings attribute
	 * 
	 * @return The value of the execStrings attribute
	 */
	public List<String> getExecStrings() {
		return execStrings;
	}

	/**
	 * Changes the value of the execStrings attribute
	 * 
	 * @param execStrings
	 *            attribute
	 */
	public void setExecStrings(final List<String> execStrings) {
		this.execStrings = execStrings;
	}

	/**
	 * Getter of the toExecute attribute
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
	 * Creates a clone of a {@link Program}
	 */
	@Override
	public Program clone() {
		try {
			return (Program) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}