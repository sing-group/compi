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

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.dk.hub.BasicAuth;
import org.sing_group.compi.dk.hub.InitHub;
import org.sing_group.compi.dk.hub.PipelineExistsException;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class HubInitCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(HubInitCommand.class.getName());

  public String getName() {
    return "hub-init";
  }

  public String getDescriptiveName() {
    return "Create compi-hub pipeline";
  }

  public String getDescription() {
    return "Creates a new pipeline at compi-hub";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getProjectPathOption(),
      getAliasOption(),
      getTitleOption(),
      getVisibleOption(),
      getForceOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, PROJECT_PATH_DESCRIPTION, PROJECT_PATH_DEFAULT_VALUE
    );
  }

  private Option<?> getAliasOption() {
    return new StringOption("alias", "a", "Alias of the pipeline", false, true);
  }

  private Option<?> getTitleOption() {
    return new StringOption("title", "t", "Title of the pipeline", false, true);
  }

  private Option<?> getVisibleOption() {
    return new FlagOption("visible", "v", "Whether the pipeline is publicly visible or not");
  }
  
  private Option<?> getForceOption() {
    return new FlagOption(
      "force", "f",
      "Whether the pipeline alias should be overriden. Note that this option will create a new pipeline at compi-hub and update the alias associated to this project"
    );
  }

  @Override
  public void execute(final Parameters parameters) throws Exception {
    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
    LOGGER.info("Building project in directory: " + directory);

    if (!directory.exists()) {
      LOGGER.severe("Directory " + directory + " does not exist");
      System.exit(1);
    }

    File compiProjectFile = new File(directory + File.separator + ProjectConfiguration.COMPI_PROJECT_FILENAME);
    if (!compiProjectFile.exists()) {
      LOGGER.severe("Compi project file does not exist: " + compiProjectFile);
      System.exit(1);
    }
    
    PropertiesFileProjectConfiguration projectConfiguration = new PropertiesFileProjectConfiguration(compiProjectFile);
    boolean force = parameters.hasOption(super.getOption("force"));
    
    if (projectConfiguration.getHubAlias() != null && !force) {
      LOGGER.warning(
        "A pipeline has been already created with alias " + projectConfiguration.getHubAlias()
          + ". Use -f/--force if you want to create a new pipeline at compi-hub"
      );
      System.exit(1);
    }

    Console console = System.console();
    String username = console.readLine("Username: ");
    String password = new String(console.readPassword("Password: "));

    String alias = parameters.getSingleValueString(super.getOption("alias"));
    String title = parameters.getSingleValueString(super.getOption("title"));
    boolean visible = parameters.hasOption(super.getOption("visible"));

    try {
      new InitHub(new BasicAuth(username, password), alias, title, visible).initHub();
    } catch (IOException e) {
      LOGGER.severe("An error ocurred while creating the pipeline at compi-hub");
      e.printStackTrace();
      System.exit(1);
    } catch (PipelineExistsException e) {
      LOGGER.severe("A pipeline already exists with the specified alias");
      System.exit(1);
    }

    projectConfiguration.setProjectAlias(alias);
    projectConfiguration.save();
  }
}
