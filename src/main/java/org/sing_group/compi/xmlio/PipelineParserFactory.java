package org.sing_group.compi.xmlio;

public class PipelineParserFactory {

  public static PipelineParser createPipelineParser() {
    return new DOMPipelineParser();
    //    return new JAXBPipelineParser();
  }
}
