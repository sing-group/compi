package org.sing_group.compi.xmlio.runners;

import java.io.File;
import java.io.IOException;

import org.sing_group.compi.xmlio.entities.runners.Runners;

public interface RunnersParser {
  public Runners parseXML(File f) throws IllegalArgumentException, IOException;
}
