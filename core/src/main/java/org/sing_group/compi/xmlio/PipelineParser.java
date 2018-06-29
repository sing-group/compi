package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;

import org.sing_group.compi.xmlio.entities.Pipeline;

public interface PipelineParser {

  public Pipeline parsePipeline(File f) throws IllegalArgumentException, IOException;
}
