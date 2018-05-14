package org.sing_group.compi.xmlio.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "param")
public class ParameterDescription implements Cloneable {

  @XmlAttribute
  private String name;
  @XmlAttribute
  private String shortName;
  @XmlValue
  private String description;
  @XmlAttribute
  private String defaultValue;

  public ParameterDescription() {}

  /**
   * Getter of the name attribute
   * 
   * @return The value of the name attribute
   */
  public String getName() {
    return name;
  }

  /**
   * Getter of the shortName attribute
   * 
   * @return The value of the shortName attribute
   */
  public String getShortName() {
    return shortName;
  }

  /**
   * Getter of the description attribute
   * 
   * @return The value of the description attribute
   */
  public String getDescription() {
    return description;
  }

  /**
   * Getter of the defaultValue attribute
   * 
   * @return The value of the defaultValue attribute
   */
  public String getDefaultValue() {
    return defaultValue;
  }
}
