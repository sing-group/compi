/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.dk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractPropertiesFile {
  private Properties properties;
  private File file;

  public AbstractPropertiesFile(File file) {
    this(file, new Properties());
  }

  public AbstractPropertiesFile(File file, Properties properties) {
    this.properties = properties;
    this.file = file;
    if (file.exists()) {
      this.loadPropertiesFromFile();
    }
  }

  private void loadPropertiesFromFile() {
    try (FileInputStream in = new FileInputStream(this.file)) {
      this.properties.load(in);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void save() {
    try (FileOutputStream out = new FileOutputStream(file)) {
      this.properties.store(out, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getProperty(String key) {
    return getProperty(key, null);
  }

  protected String getProperty(String key, String defaultValue) {
    return this.properties.getProperty(key, defaultValue);
  }

  protected void setProperty(String key, String value) {
    this.properties.setProperty(key, value);
  }
}
