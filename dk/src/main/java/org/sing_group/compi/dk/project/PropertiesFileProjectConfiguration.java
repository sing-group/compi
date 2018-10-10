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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFileProjectConfiguration implements ProjectConfiguration {

  public static final String IMAGE_NAME_PROPERTY = "image.name";
  public static final String COMPI_VERSION_PROPERTY = "compi.version";

  private Properties prop = new Properties();
  private File configFile;

  public PropertiesFileProjectConfiguration(File configFile) {
    this.configFile = configFile;
    if (configFile.exists()) {
      read();
    }
  }


  @Override
  public String getImageName() {
    return prop.getProperty(IMAGE_NAME_PROPERTY);
  }

  @Override
  public String getCompiVersion() {
    return prop.getProperty(COMPI_VERSION_PROPERTY);
  }

  public void setImageName(String imageName) {
    prop.setProperty(IMAGE_NAME_PROPERTY, imageName);
  }

  public void setCompiVersion(String compiVersion) {
    prop.setProperty(COMPI_VERSION_PROPERTY, compiVersion);
  }

  public void save() {
    try (FileOutputStream out = new FileOutputStream(configFile)) {
      prop.store(out, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void read() {
    try (FileInputStream in = new FileInputStream(this.configFile)) {
      this.prop = new Properties();
      this.prop.load(in);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
