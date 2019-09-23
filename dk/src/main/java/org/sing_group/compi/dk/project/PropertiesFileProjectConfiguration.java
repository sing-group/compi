/*-
 * #%L
 * Compi Development Kit
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
