/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2020 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiTaskAbortedException;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Task;

public class CLITaskExecutionHandler implements TaskExecutionHandler {
  private static final Logger LOGGER = getLogger(CLITaskExecutionHandler.class.getName());
  private Set<String> startedForeachs = new HashSet<String>();

  @Override
  synchronized public void taskStarted(Task task) {
    if (task instanceof Foreach) {
      if (!startedForeachs.contains(task.getId())) {
        LOGGER.info(
          "> Started loop task " + task.getId()
        );
        startedForeachs.add(task.getId());
      }
    } else {
      LOGGER.info("> Started task " + task.getId());
    }
  }

  @Override
  synchronized public void taskFinished(Task task) {
    if (task instanceof Foreach) {
      LOGGER.info(
        "< Finished loop task " + task.getId()
      );
    } else {
      LOGGER.info("< Finished task " + task.getId());
    }
  }

  @Override
  synchronized public void taskAborted(Task task, CompiTaskAbortedException e) {
    LOGGER.severe(
      "X Aborted task " + task.getId() + ". Cause: " + e.getMessage() + getLogInfo(e)
    );
  }

  @Override
  public void taskIterationStarted(ForeachIteration iteration) {
    LOGGER.info(
      ">> Started loop iteration of task " + iteration.getId() + " (" + iteration.getIterationValue() + ")"
    );
  }

  @Override
  public void taskIterationFinished(ForeachIteration iteration) {
    LOGGER.info(
      "<< Finished loop iteration of task " + iteration.getId() + " (" + iteration.getIterationValue() + ")"
    );
  }

  @Override
  public void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {
    LOGGER.severe(
      "X Aborted loop iteration of task " + iteration.getId() + " (" + iteration.getIterationValue() + ")" + ". Cause: "
        + e.getMessage() + getLogInfo(e)
    );
  }

  private String getLogInfo(CompiTaskAbortedException e) {
    StringBuilder builder = new StringBuilder();

    if (e.getLastStdOut().size() > 0) {
      builder.append("\n");
      builder.append("--- Last " + e.getLastStdOut().size() + " stdout lines ---\n");
      e.getLastStdOut().forEach(line -> {
        builder.append(line + "\n");
      });
      builder.append("--- End of stdout lines ---\n");
    }

    if (e.getLastStdErr().size() > 0) {
      builder.append("\n");
      builder.append("--- Last " + e.getLastStdErr().size() + " stderr lines ---\n");
      e.getLastStdErr().forEach(line -> {
        builder.append(line + "\n");
      });
      builder.append("--- End of stderr lines ---\n");
    }

    if (e.getLastStdOut().size() > 0 && e.getTask().getStdOutLogFile() != null) {
      builder.append("\nComplete stdout log file: " + e.getTask().getStdOutLogFile());
    } else if (e.getLastStdOut().size() > 0) {
      builder.append(
        "\nTask " + e.getTask().getId()
          + " was not recording the complete stdout in a log file. Enable logging if you want and run again. "
      );
    }
    if (e.getLastStdErr().size() > 0 && e.getTask().getStdErrLogFile() != null) {
      builder.append("\nComplete stderr log file: " + e.getTask().getStdErrLogFile());
    } else if (e.getLastStdErr().size() > 0) {
      builder.append(
        "\nTask " + e.getTask().getId()
          + " was not recording the complete stderr in a log file. Enable logging if you want and run again. "
      );
    }
    return builder.toString();
  }
}
