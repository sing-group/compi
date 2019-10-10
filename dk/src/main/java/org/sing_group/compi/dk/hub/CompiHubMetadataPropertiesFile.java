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
package org.sing_group.compi.dk.hub;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.Console;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.dk.AbstractPropertiesFile;
import org.sing_group.compi.dk.cli.WriteOnlyProperties;

public class CompiHubMetadataPropertiesFile extends AbstractPropertiesFile {
  public static final String COMPI_HUB_METADATA_FILENAME = "hub.metadata";
  private static final String NOT_AVAILABLE = "not set";

  private static final List<MetadataProperty> PROPERTIES =
    asList(
      new MetadataProperty("url.dockerhub", "Docker Hub URL"),
      new MetadataProperty("url.github", "GitHub URL"),
      new MetadataProperty("url.bitbucket", "Bitbucket URL"),
      new MetadataProperty("url.gitlab", "GitLab URL")
    );

  public CompiHubMetadataPropertiesFile(File file) {
    super(file, new WriteOnlyProperties());
  }

  public void readProperties(Logger logger, Console console) {
    for (MetadataProperty property : PROPERTIES) {
      String current = this.getProperty(property.key(), NOT_AVAILABLE);
      logger.info(format("%s (current value: %s)", property.name(), current));
      String newValue =
        console.readLine(format("New %s (leave it empty to keep the previous value): ", property.name()));
      newValue = newValue.trim();

      if (!newValue.isEmpty()) {
        this.setProperty(property.key(), newValue);
      }
    }
  }
}
