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
package org.sing_group.compi.dk.cli;

import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;
import static org.sing_group.compi.dk.hub.CompiHubMetadataPropertiesFile.COMPI_HUB_METADATA_FILENAME;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.dk.hub.CompiHubMetadataPropertiesFile;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class HubMetadataCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(HubMetadataCommand.class.getName());

  public String getName() {
    return "hub-metadata";
  }

  public String getDescriptiveName() {
    return "Sets the compi-hub pipeline version metadata";
  }

  public String getDescription() {
    return "Sets the compi-hub pipeline version metadata";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getProjectPathOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, PROJECT_PATH_DESCRIPTION, PROJECT_PATH_DEFAULT_VALUE
    );
  }

  @Override
  public void execute(final Parameters parameters) throws Exception {
    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
    LOGGER.info("Adding metadata to compi-dk project in directory: " + directory);

    if (!directory.exists()) {
      LOGGER.severe("Directory " + directory + " does not exist");
      System.exit(1);
    }

    File hubMetadataFile = new File(directory + File.separator + COMPI_HUB_METADATA_FILENAME);

    CompiHubMetadataPropertiesFile hubMetadata = new CompiHubMetadataPropertiesFile(hubMetadataFile);

    hubMetadata.readProperties(LOGGER, System.console());

    hubMetadata.save();
  }
}
