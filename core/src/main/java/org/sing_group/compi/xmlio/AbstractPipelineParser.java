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
