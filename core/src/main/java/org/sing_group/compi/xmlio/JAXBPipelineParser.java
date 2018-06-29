package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sing_group.compi.xmlio.entities.Pipeline;

public class JAXBPipelineParser extends AbstractPipelineParser {

  @Override
  public Pipeline parseXML(File f) throws IllegalArgumentException, IOException {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(Pipeline.class);
      final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      Pipeline pipeline = (Pipeline) jaxbUnmarshaller.unmarshal(f);
      return pipeline;
      
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    } 
   

  }

}
