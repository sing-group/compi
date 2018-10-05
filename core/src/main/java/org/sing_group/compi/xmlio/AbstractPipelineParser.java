package org.sing_group.compi.xmlio;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.xmlio.entities.Pipeline;

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
        globalParameters.forEach(task::addParameter);
      }
    });
  }
}
