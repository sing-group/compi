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
package org.sing_group.compi.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;

/**
 * This class saves the execution status of a Compi pipeline
 * 
 */
public class CompiExecutionLog implements Serializable {

  private static final String REDO_LOG_EXTENSION = ".redo.log";
  private static final File COMPI_LOG_DIR = new File(System.getProperty("user.home") + File.separator + ".compi");
  private static final long serialVersionUID = 1L;

  private long timestamp = new Date().getTime();

  private final Set<Task> finishedTasks = new HashSet<>();

  private final File logFile;

  private final CompiRunConfiguration config;

  private CompiExecutionLog(final CompiRunConfiguration configuration, final File logFile) throws IOException {
    this.config = configuration;
    this.logFile = logFile;
    this.save();
  }

  public CompiExecutionLog(final CompiRunConfiguration configuration) throws IOException {
    this(configuration, getNewLogFile(configuration.getPipelineFile()));
  }

  private static File getNewLogFile(File pipelineFile) {
    if (!COMPI_LOG_DIR.exists()) {
      COMPI_LOG_DIR.mkdirs();
    }
    return createLogFile(pipelineFile);
  }

  private static File createLogFile(File pipelineFile) {
    if (pipelineFile == null) {
      return new File(COMPI_LOG_DIR + File.separator + "" + UUID.randomUUID().toString() + REDO_LOG_EXTENSION);
    }
    return new File(
      COMPI_LOG_DIR + File.separator + "" + UUID.nameUUIDFromBytes(pipelineFile.getAbsolutePath().getBytes()) + REDO_LOG_EXTENSION
    );
  }

  public CompiRunConfiguration getCompiRunConfiguration() {
    return this.config;
  }

  public File getLogFile() {
    return this.logFile;
  }

  public Set<Task> getFinishedTasks() {
    return this.finishedTasks;
  }

  public static CompiExecutionLog forPipeline(final File pipelineFile) throws Exception {

    final File logFile = createLogFile(pipelineFile);

    if (!logFile.exists()) {
      throw new IllegalArgumentException(
        "Cannot find redo log for pipeline file: " + pipelineFile + ". Log should be in: " + logFile
      );
    }
    return load(pipelineFile, logFile);
  }

  private static CompiExecutionLog load(final File pipelineFile, final File logFile)
    throws IOException, ClassNotFoundException, FileNotFoundException, IllegalArgumentException,
    PipelineValidationException {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(logFile))) {
      CompiExecutionLog log = (CompiExecutionLog) ois.readObject();
      if (log.config.getPipelineFile().equals(pipelineFile)) {
        log.config.setPipeline(Pipeline.fromFile(log.config.getPipelineFile()));
        return log;
      } else {
        throw new IllegalArgumentException(
          "Found log file for pipeline at: " + pipelineFile + " , but it is for a different pipeline file: "
            + log.config.getPipelineFile()
        );
      }
    }
  }

  public void taskFinished(final Task t) throws IOException {
    this.finishedTasks.add(t);

    this.save();
  }

  public boolean taskWasFinished(final Task t) {
    for (final Task task : this.finishedTasks) {
      if (task.getId().equals(t.getId())) {
        if (
          task instanceof ForeachIteration && t instanceof ForeachIteration &&
            ((ForeachIteration) task).getIterationIndex() == ((ForeachIteration) t).getIterationIndex()
        ) {
          return true;
        } else if (task.getClass().equals(t.getClass()) && !(t instanceof ForeachIteration)) {
          return true;
        }
      }
    }
    return false;
  }

  private void save() throws IOException {
    this.timestamp = new Date().getTime();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(this.logFile))) {
      oos.writeObject(this);
    }
  }
}
