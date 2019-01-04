/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.core.pipeline;

public class ParameterDescription implements Cloneable {

  
  private String name;
  
  private String shortName;
  
  private String description;
  
  private String defaultValue;
  
  private boolean global;
  
  private boolean isFlag;

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
  
  public boolean isFlag() {
    return isFlag;
  }

  public void setFlag(boolean isFlag) {
    this.isFlag = isFlag;
  }
}
