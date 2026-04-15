/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2025 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.dk.module;

import java.io.IOException;
import java.util.List;

/**
 * Interface for Compi DK modules that can be added to projects.
 * 
 * @author hlfernandez
 *
 */
public interface CompiDkModule {

  /**
   * Returns the module name.
   * 
   * @return the module name
   */
  String getName();

  /**
   * Returns the module description.
   * 
   * @return the module description
   */
  String getDescription();

  /**
   * Returns the module version.
   * 
   * @return the module version
   */
  String getVersion();

  /**
   * Returns the XML content to insert inside the &lt;tasks&gt;&lt;/tasks&gt;
   * section of pipeline.xml.
   * 
   * @return the tasks XML content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getTasks() throws IOException;

  /**
   * Returns the XML content to insert inside the
   * &lt;metadata&gt;&lt;/metadata&gt; section of pipeline.xml.
   * 
   * @return the metadata XML content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getMetadata() throws IOException;

  /**
   * Returns the XML content to insert inside the
   * &lt;parameters&gt;&lt;/parameters&gt; section of pipeline.xml.
   * 
   * @return the parameters XML content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getParams() throws IOException;

  /**
   * Returns the XML content to insert inside the
   * &lt;runners&gt;&lt;/runners&gt; section of runners.xml.
   * 
   * @return the runners XML content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getRunners() throws IOException;

  /**
   * Returns a list of paths to ZIP files to uncompress in the project root.
   * These can include both bundled ZIP files, ZIP files created from directory
   * structures, and downloaded ZIP files from URLs.
   * 
   * @return the list of ZIP file paths
   * @throws IOException
   *           if an I/O error occurs
   */
  List<String> getFiles() throws IOException;

  /**
   * Returns the content to insert in the project Dockerfile.
   * 
   * @return the Dockerfile content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getDockerfile() throws IOException;

  /**
   * Returns the license text of the module.
   * 
   * @return the license content
   * @throws IOException
   *           if an I/O error occurs
   */
  String getLicense() throws IOException;
}