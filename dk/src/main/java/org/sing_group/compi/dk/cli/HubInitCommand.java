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
package org.sing_group.compi.dk.cli;

import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.dk.hub.BasicAuth;
import org.sing_group.compi.dk.hub.InitHub;
import org.sing_group.compi.dk.hub.InitHubResponse;
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
    File directory = new File((String) parameters.getSingleValue(this.getOption(PROJECT_PATH)));
    LOGGER.info("Initializing pipeline at compi-hub from directory: " + directory);

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

    BasicAuth basicAuth = BasicAuth.fromConsole(System.console());

    String alias = parameters.getSingleValueString(super.getOption("alias"));
    String title = parameters.getSingleValueString(super.getOption("title"));
    boolean visible = parameters.hasOption(super.getOption("visible"));

    try {
      InitHubResponse response = new InitHub(basicAuth, alias, title, visible).initHub();

      projectConfiguration.setHubAlias(alias);
      projectConfiguration.settHubId(response.getHubId());
      projectConfiguration.save();
    } catch (IOException e) {
      LOGGER.severe("An error ocurred while creating the pipeline at compi-hub");
      e.printStackTrace();
      System.exit(1);
    } catch (PipelineExistsException e) {
      LOGGER.severe("A pipeline already exists with the specified alias");
      System.exit(1);
    }
  }
}
