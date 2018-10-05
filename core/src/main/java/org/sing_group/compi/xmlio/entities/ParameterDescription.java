package org.sing_group.compi.xmlio.entities;

public class ParameterDescription implements Cloneable {

  
  private String name;
  
  private String shortName;
  
  private String description;
  
  private String defaultValue;
  
  private boolean global;

  public ParameterDescription() {}

  /**
   * Getter of the name attribute
   * 
   * @return The value of the name attribute
   */
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
  public String getDefaultValue() {
    return defaultValue;
  }
  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public boolean isGlobal() {
    return global;
  }
  
  public void setGlobal(boolean global) {
    this.global = global;
  }
}
