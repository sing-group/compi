/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sing_group.compi.core.pipeline;

import java.io.Serializable;

public class ParameterDescription implements Cloneable, Serializable {
  private static final long serialVersionUID = 1L;

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
