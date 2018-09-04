package org.sing_group.compi.xmlio.entities.runners;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "runner")
public class Runner {

  private String tasks;
  private String runnerCode;
  
  @XmlAttribute
  public String getTasks() {
    return tasks;
  }
  
  public void setTasks(String tasks) {
    this.tasks = tasks;
  }
  
  @XmlValue
  public String getRunnerCode() {
    return runnerCode;
  }
  
  public void setRunnerCode(String runnerCode) {
    this.runnerCode = runnerCode;
  }
}
