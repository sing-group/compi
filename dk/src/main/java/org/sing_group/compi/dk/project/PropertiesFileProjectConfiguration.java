/*-
 * #%L
 * Compi Development Kit
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
package org.sing_group.compi.dk.project;

import java.io.File;

import org.sing_group.compi.dk.AbstractPropertiesFile;

public class PropertiesFileProjectConfiguration extends AbstractPropertiesFile implements ProjectConfiguration {
  public static final String IMAGE_NAME_PROPERTY = "image.name";
  public static final String COMPI_VERSION_PROPERTY = "compi.version";
  public static final String COMPI_HUB_PIPELINE_ALIAS = "hub.pipeline.alias";
  public static final String COMPI_HUB_PIPELINE_ID = "hub.pipeline.id";

  public PropertiesFileProjectConfiguration(File configFile) {
    super(configFile);
  }

  @Override
  public String getImageName() {
    return getProperty(IMAGE_NAME_PROPERTY);
  }

  @Override
  public String getCompiVersion() {
    return getProperty(COMPI_VERSION_PROPERTY);
  }

  public void setImageName(String imageName) {
    setProperty(IMAGE_NAME_PROPERTY, imageName);
  }

  public void setCompiVersion(String compiVersion) {
    setProperty(COMPI_VERSION_PROPERTY, compiVersion);
  }

  public void setHubAlias(String alias) {
    setProperty(COMPI_HUB_PIPELINE_ALIAS, alias);
  }

  @Override
  public String getHubAlias() {
    return getProperty(COMPI_HUB_PIPELINE_ALIAS);
  }

  public void settHubId(String hubId) {
    setProperty(COMPI_HUB_PIPELINE_ID, hubId);
  }

  @Override
  public String getHubId() {
    return getProperty(COMPI_HUB_PIPELINE_ID);
  }
}
