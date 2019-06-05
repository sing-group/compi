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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;

/**
 * Definition of a compi pipeline.
 *
 * @author Jesus Alvarez Casanova
 * @author Hugo López-Fernández
 *
 */
public class Pipeline {
  private String version = "";
  private List<Task> tasks = new LinkedList<>();
  private List<ParameterDescription> parameterDescriptions = new LinkedList<>();

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(final List<Task> tasks) {
    this.tasks = tasks;

  }

  public List<ParameterDescription> getParameterDescriptions() {
    return parameterDescriptions;
  }

  public void setParameterDescriptions(List<ParameterDescription> parameterDescriptions) {
    this.parameterDescriptions = parameterDescriptions;
  }

  /**
   * Returns all parameter names in the pipeline, organized by each task id
   *
   * @return a map of task ids to a list of parameter names
   */
  public Map<String, List<String>> getParametersByTask() {
    return this.tasks.stream().collect(toMap(t -> t.getId(), t -> t.getParameters()));
  }

  /**
   * Returns a mapping of configurable parameters to tasks using them
   *
   * @return a mapping of configurable parameters to tasks using them
   */
  public Map<String, List<Task>> getTasksByParameter() {
    return this.getAllParameters().stream().collect(
      toMap(
        t -> t,
        p -> this.tasks.stream().filter(task -> task.getParameters().contains(p)).collect(toList())
      )
    );
  }

  private Set<String> getAllParameters() {
    return this.tasks.stream()
      .map(
        p -> p.getParameters().stream()
          .filter(param -> !(p instanceof Foreach) || !((Foreach) p).getAs().equals(param))
          .collect(toList())
      )
      .flatMap(x -> x.stream()).collect(toSet());
  }

  /**
   * Returns the {@link ParameterDescription} for a given a parameter name
   *
   * @param parameterName the parameter name
   * @return the parameter description, null if it is not available
   */
  public ParameterDescription getParameterDescription(String parameterName) {
    return this.getParameterDescriptions().stream().collect(toMap(p -> p.getName(), p -> p)).get(parameterName);
  }

  public static Pipeline fromFile(File pipelineFile)
    throws IllegalArgumentException, IOException, PipelineValidationException {
    return fromFile(pipelineFile, null);
  }

  public static Pipeline fromFile(File pipelineFile, List<ValidationError> errors)
    throws IllegalArgumentException, IOException, PipelineValidationException {
    return validateAndCreatePipeline(pipelineFile, errors);
  }

  private static Pipeline validateAndCreatePipeline(final File pipelineFile, List<ValidationError> errors)
    throws PipelineValidationException {
    PipelineValidator validator = new PipelineValidator(pipelineFile);

    if (errors == null) {
      errors = new ArrayList<>();
    } else {
      errors.clear();
    }

    errors.addAll(validator.validate());

    if (errors.stream().filter(error -> error.getType().isError()).count() > 0) {
      throw new PipelineValidationException(errors);
    }
    return validator.getPipeline();
  }
}
