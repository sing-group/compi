package org.sing_group.compi.xmlio.entities.runners;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "runners")
public class Runners {

  private List<Runner> runners;

  @XmlElement(name = "runner", type=Runner.class)
  public List<Runner> getRunners() {
    return runners;
  }
  
  public void setRunners(List<Runner> runners) {
    this.runners = runners;
  }
}
