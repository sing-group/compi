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
package org.sing_group.compi.xmlio;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.pipeline.Pipeline;

public abstract class AbstractPipelineParser implements PipelineParser {

  @Override
  public Pipeline parsePipeline(File f) throws IllegalArgumentException, IOException {
    Pipeline pipeline = parseXML(f);

    addTaskParameters(pipeline);
    return pipeline;
  }

  protected abstract Pipeline parseXML(File f) throws IllegalArgumentException, IOException;

  private static void addTaskParameters(Pipeline pipeline) {
    final List<String> globalParameters = new LinkedList<>();
    pipeline.getParameterDescriptions().forEach(parameterDescription -> {
      if (parameterDescription.isGlobal()) {
        globalParameters.add(parameterDescription.getName());
      }
    });

    pipeline.getTasks().forEach(task -> {
      if (task.getParametersString() != null && task.getParametersString().trim().length() > 0) {
        asList(task.getParametersString().trim().split("\\s+")).forEach(task::addParameter);
      }
      globalParameters.forEach(task::addParameter);
    });
  }
}
