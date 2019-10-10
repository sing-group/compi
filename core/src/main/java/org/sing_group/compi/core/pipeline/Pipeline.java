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
