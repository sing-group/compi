/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sing_group.compi.core.pipeline;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.CompiTaskAbortedException;

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
  private String interpreter;
  private String runIf;
  private String src;
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
  private CompiTaskAbortedException abortionCause;

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

  public String getSrc() {
    return src;
  }

  public void setSrc(String src) {
    this.src = src;
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

  public void setAborted(final boolean isAborted, CompiTaskAbortedException abortionCause) {
    this.isAborted = isAborted;
    this.abortionCause = abortionCause;
  }

  public CompiTaskAbortedException getAbortionCause() {
    return abortionCause;
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
