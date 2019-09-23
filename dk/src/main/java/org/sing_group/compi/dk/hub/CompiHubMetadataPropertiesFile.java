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
