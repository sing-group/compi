package es.uvigo.esei.compi.xmlio.entities;

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
	 * Getter of the id global variable
	 * 
	 * @return The value of the id global variable
	 */
	@XmlAttribute
	public String getId() {
		return id;
	}

	/**
	 * Changes the value of the id global variable
	 * 
	 * @param id
	 *            Global variable
	 */
	public void setId(final String id) {
		this.id = id.replaceAll(" ", "");
	}

	/**
	 * Getter of the dependsOn global variable
	 * 
	 * @return The value of the dependsOn global variable
	 */
	@XmlAttribute
	public String getDependsOn() {
		return dependsOn;
	}

	/**
	 * Changes the value of the dependsOn global variable
	 * 
	 * @param dependsOn
	 *            Global variable
	 */
	public void setDependsOn(final String dependsOn) {
		this.dependsOn = dependsOn.replaceAll(" ", "");
	}

	/**
	 * Getter of the fileLog global variable
	 * 
	 * @return The value of the fileLog global variable
	 */
	@XmlAttribute
	public String getFileLog() {
		return fileLog;
	}

	/**
	 * Changes the value of the fileLog global variable
	 * 
	 * @param fileLog
	 *            Global variable
	 */
	public void setFileLog(final String fileLog) {
		this.fileLog = fileLog;
	}

	/**
	 * Getter of the fileErrorLog global variable
	 * 
	 * @return The value of the fileErrorLog global variable
	 */
	@XmlAttribute
	public String getFileErrorLog() {
		return fileErrorLog;
	}

	/**
	 * Changes the value of the fileErrorLog global variable
	 * 
	 * @param fileErrorLog
	 *            Global variable
	 */
	public void setFileErrorLog(final String fileErrorLog) {
		this.fileErrorLog = fileErrorLog;
	}

	/**
	 * Getter of the foreach global variable
	 * 
	 * @return The value of the foreach global variable
	 */
	@XmlElement
	public Foreach getForeach() {
		return foreach;
	}

	/**
	 * Changes the value of the foreach global variable
	 * 
	 * @param foreach
	 *            Global variable
	 */
	public void setForeach(final Foreach foreach) {
		this.foreach = foreach;
	}

	/**
	 * Getter of the exec global variable
	 * 
	 * @return The value of the exec global variable
	 */
	@XmlElement
	public String getExec() {
		return exec;
	}

	/**
	 * Changes the value of the exec global variable
	 * 
	 * @param exec
	 *            Global variable
	 */
	public void setExec(final String exec) {
		this.exec = exec.trim();
		this.toExecute = this.exec;
	}

	/**
	 * Getter of the isRunning global variable
	 * 
	 * @return The value of the isRunning global variable
	 */
	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Changes the value of the isRunning global variable
	 * 
	 * @param isRunning
	 *            Global variable
	 */
	public void setRunning(final boolean isRunning) {
		this.isRunning = isRunning;
	}

	/**
	 * Getter of the isFinished global variable
	 * 
	 * @return The value of the isFinished global variable
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Changes the value of the isFinished global variable
	 * 
	 * @param isFinished
	 *            Global variable
	 */
	public void setFinished(final boolean isFinished) {
		this.isFinished = isFinished;
	}

	/**
	 * Getter of the execStrings global variable
	 * 
	 * @return The value of the execStrings global variable
	 */
	public List<String> getExecStrings() {
		return execStrings;
	}

	/**
	 * Changes the value of the execStrings global variable
	 * 
	 * @param execStrings
	 *            Global variable
	 */
	public void setExecStrings(final List<String> execStrings) {
		this.execStrings = execStrings;
	}

	/**
	 * Getter of the toExecute global variable
	 * 
	 * @return The value of the toExecute global variable
	 */
	public String getToExecute() {
		return toExecute;
	}

	/**
	 * Changes the value of the toExecute global variable
	 * 
	 * @param toExecute
	 *            Global variable
	 */
	public void setToExecute(final String toExecute) {
		this.toExecute = toExecute;
	}

	/**
	 * Getter of the isAborted global variable
	 * 
	 * @return The value of the isAborted global variable
	 */
	public boolean isAborted() {
		return isAborted;
	}

	/**
	 * Changes the value of the isAborted global variable
	 * 
	 * @param isAborted
	 *            Global variable
	 */
	public void setAborted(final boolean isAborted) {
		this.isAborted = isAborted;
	}

	/**
	 * Getter of the isSkipped global variable
	 * 
	 * @return The value of the isSkipped global variable
	 */
	public boolean isSkipped() {
		return isSkipped;
	}

	/**
	 * Changes the value of the isSkipped global variable
	 * 
	 * @param isSkipped
	 *            Global variable
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