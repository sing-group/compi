package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

public abstract class AbstractPipelineParser implements PipelineParser {

  @Override
  public Pipeline parsePipeline(File f) throws IllegalArgumentException, IOException {
    Pipeline pipeline = parseXML(f);

    addTaskParameters(pipeline);
    return pipeline;
  }

  protected abstract Pipeline parseXML(File f) throws IllegalArgumentException, IOException;

  /**
   * Obtain the content inside ${...} in the {@link Task} exec tag
   * 
   * @param pipeline Contains all the {@link Task}
   */
  private static void addTaskParameters(Pipeline pipeline) {

    pipeline.getTasks().forEach(task -> {
      if (task.getParametersString() != null && task.getParametersString().trim().length() > 0) {
          Arrays.asList(task.getParametersString().trim().split(",")).forEach(parameterName -> {
            parameterName = parameterName.trim();

            if (pipeline.getParameterDescription(parameterName) != null) {
              task.addParameter(parameterName);
            } else {
              throw new IllegalArgumentException("parameter " + parameterName + " not declared");
            }
          });
      }
    });
  }

}
