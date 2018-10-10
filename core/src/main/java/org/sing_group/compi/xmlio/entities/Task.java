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
package org.sing_group.compi.xmlio.entities;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a {@link Task} obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class Task implements Cloneable {

	private String id;
	private String after;
	private String exec;
	private String interpreter;
	private List<String> parameters = new LinkedList<>();
	private String parametersString;
	private String toExecute;
	private boolean isRunning = false;
	private boolean isFinished = false;
	private boolean isAborted = false;
	private boolean isSkipped = false;
	private File stdOutLogFile, stdErrLogFile;

	/**
	 * Getter of the id attribute
	 * 
	 * @return The value of the id attribute
	 */
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
	public String getAfter() {
		return after;
	}
	
	/**
   * Getter of the interpreter attribute
   * 
   * @return The value of the interpreter attribute
   */
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
	
	public String getParametersString() {
    return parametersString;
  }

	/**
	 * Getter of the exec attribute
	 * 
	 * @return The value of the exec attribute
	 */
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
	 * Getter of the parameters attribute
	 * 
	 * @return The value of the paramters attribute
	 */
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

	
	public File getStdOutLogFile() {
    return stdOutLogFile;
  }

  public void setStdOutLogFile(File stdOutLogFile) {
    this.stdOutLogFile = stdOutLogFile;
  }

  public File getStdErrLogFile() {
    return stdErrLogFile;
  }

  public void setStdErrLogFile(File stdErrLogFile) {
    this.stdErrLogFile = stdErrLogFile;
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
	
	@Override
	public String toString() {
	  return "Task[class: "+this.getClass().getSimpleName()+" id: "+this.id+"]";
	}

}
