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
package org.sing_group.compi.core.pipeline;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Definition of a compi pipeline task.
 * 
 * @author Jesus Alvarez Casanova
 * @author Hugo López-Fernández
 *
 */
public class Task implements Cloneable {

  private String id;
  private String after;
  private String exec;
  private String interpreter;
  private String runIf;
  private List<String> parameters = new LinkedList<>();
  private String parametersString;
  private String toExecute;
  private boolean isRunning = false;
  private boolean isFinished = false;
  private boolean isAborted = false;
  private boolean isSkipped = false;
  private File stdOutLogFile, stdErrLogFile;
  private Pipeline pipeline;
  private TaskMetadata metadata;

  public Task(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id.trim();
  }

  public String getAfter() {
    return after;
  }
  
  public List<String> getAfterList() {
    if (this.after != null) {
      return asList(this.after.split("[\\s,]+"));
    } else {
      return emptyList();
    }
  }

  public String getInterpreter() {
    return interpreter;
  }

  public void setInterpreter(String interpreter) {
    this.interpreter = interpreter;
  }

  public String getRunIf() {
    return runIf;
  }

  public void setRunIf(String runIf) {
    this.runIf = runIf;
  }

  public void setAfter(final String after) {
    this.after = after;
  }

  public void setParametersString(String parametersString) {
    this.parametersString = parametersString;
  }

  public String getParametersString() {
    return parametersString;
  }

  public String getExec() {
    return exec;
  }

  public void setExec(final String exec) {
    this.exec = exec.trim();
    this.toExecute = this.exec;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void setRunning(final boolean isRunning) {
    this.isRunning = isRunning;
  }

  public boolean isFinished() {
    return isFinished;
  }

  public void setFinished(final boolean isFinished) {
    this.isFinished = isFinished;
  }

  public List<String> getParameters() {
    return Collections.unmodifiableList(parameters);
  }

  public void addParameter(final String parameter) {
    this.parameters.add(parameter);
  }

  public String getToExecute() {
    return toExecute;
  }

  public void setToExecute(final String toExecute) {
    this.toExecute = toExecute;
  }

  public boolean isAborted() {
    return isAborted;
  }

  public void setAborted(final boolean isAborted) {
    this.isAborted = isAborted;
  }

  public boolean isSkipped() {
    return isSkipped;
  }

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

  public void setMetadata(TaskMetadata metadata) {
    this.metadata = metadata;
  }

  public TaskMetadata getMetadata() {
    return metadata;
  }

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
    return "Task[class: " + this.getClass().getSimpleName() + " id: " + this.id + "]";
  }
}
