package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sing_group.compi.xmlio.entities.Foreach;
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
   * @param pipeline
   *          Contains all the {@link Task}
   */
  private static void addTaskParameters(Pipeline pipeline) {
    final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

    for (final Task task : pipeline.getTasks()) {
      final Matcher matcher = pattern.matcher(task.getExec());
      while (matcher.find()) {
        task.addParameter(matcher.group(1));
      }
      if (task instanceof Foreach && ((Foreach) task).getOf().equals("param")) {
        task.addParameter(((Foreach) task).getIn());
      }
    }
  }

}
