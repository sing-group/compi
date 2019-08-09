/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.dk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractPropertiesFile {
  private Properties properties = new Properties();
  private File file;

  public AbstractPropertiesFile(File file) {
    this.file = file;
    if (file.exists()) {
      read();
    }
  }

  public void save() {
    try (FileOutputStream out = new FileOutputStream(file)) {
      this.properties.store(out, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void read() {
    try (FileInputStream in = new FileInputStream(this.file)) {
      this.properties = new Properties();
      this.properties.load(in);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getProperty(String key, String defaultValue) {
    return this.properties.getProperty(key, defaultValue);
  }

  protected void setProperty(String key, String value) {
    this.properties.setProperty(key, value);
  }
}
