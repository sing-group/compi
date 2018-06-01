package org.sing_group.compi.xmlio.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "param")
public class ParameterDescription implements Cloneable {

  
  private String name;
  
  private String shortName;
  
  private String description;
  
  private String defaultValue;

  public ParameterDescription() {}

  /**
   * Getter of the name attribute
   * 
   * @return The value of the name attribute
   */
  @XmlAttribute
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter of the shortName attribute
   * 
   * @return The value of the shortName attribute
   */
  @XmlAttribute
  public String getShortName() {
    return shortName;
  }
  
  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  /**
   * Getter of the description attribute
   * 
   * @return The value of the description attribute
   */
  @XmlValue
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Getter of the defaultValue attribute
   * 
   * @return The value of the defaultValue attribute
   */
  @XmlAttribute
  public String getDefaultValue() {
    return defaultValue;
  }
  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
}
