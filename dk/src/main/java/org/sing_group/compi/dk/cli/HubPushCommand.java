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

import static java.util.Collections.emptyList;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.dk.hub.BasicAuth;
import org.sing_group.compi.dk.hub.CompiHub;
import org.sing_group.compi.dk.hub.CompiHubPipelineVersion;
import org.sing_group.compi.dk.hub.CompiProjectDirectory;
import org.sing_group.compi.dk.hub.ImportVersionZip;
import org.sing_group.compi.dk.hub.PipelineExistsException;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class HubPushCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(HubPushCommand.class.getName());

  public String getName() {
    return "hub-push";
  }

  public String getDescriptiveName() {
    return "Push pipeline version";
  }

  public String getDescription() {
    return "Pushes a pipeline to compi-hub";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getProjectPathOption(),
      getVisibleOption(),
      getForceOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, PROJECT_PATH_DESCRIPTION, PROJECT_PATH_DEFAULT_VALUE
    );
  }

  private Option<?> getVisibleOption() {
    return new FlagOption("visible", "v", "If it is visible or not");
  }
  
  private Option<?> getForceOption() {
    return new FlagOption("force", "f", "If it is visible or not");
  }

  @Override
  public void execute(final Parameters parameters) {
    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
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

    BasicAuth basicAuth = BasicAuth.fromConsole(System.console());

    boolean visible = parameters.hasOption(super.getOption("visible"));
    boolean force = parameters.hasOption(super.getOption("force"));
    
    CompiProjectDirectory compiProjectDir = new CompiProjectDirectory(directory);
    
    String version = null;
    try {
      version = compiProjectDir.getPipelineVersion();
    } catch (IllegalArgumentException | IOException | PipelineValidationException e1) {
      e1.printStackTrace();
      System.exit(1);
    }
    
    String pipelineHubId = projectConfiguration.getHubId();

    if (pipelineHubId == null) {
      LOGGER
        .severe("A pipeline must be registered at compi-hub before pushing a version. Use compi-dk hub-init first.");
      System.exit(1);
    }

    List<CompiHubPipelineVersion> pipelineVersions = emptyList();
    try {
      pipelineVersions = new CompiHub(basicAuth).listPipelineVersions(pipelineHubId);
    } catch (IOException e1) {
      e1.printStackTrace();
      System.exit(1);
    }

    Optional<CompiHubPipelineVersion> compiHubVersion = existsVersion(pipelineVersions, version);
    if (compiHubVersion.isPresent()) {
      if(!force) {
        LOGGER.warning("Version " + version + " already exists at compi-hub for this pipeline. Use --force");
        System.exit(0);
      }
    }

    File zipFile = new File("/home/hlfernandez/Tmp/test-project.zip");
    try {
      compiProjectDir.toZipFile(zipFile);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    ImportVersionZip ivz = new ImportVersionZip(basicAuth, pipelineHubId, visible, zipFile);

    try {
      if (compiHubVersion.isPresent()) {
        ivz.updateZip(compiHubVersion.get());
      } else {
        ivz.uploadZip();
      }
    } catch (IOException | PipelineExistsException e) {
      e.printStackTrace();
    }
  }

  private Optional<CompiHubPipelineVersion> existsVersion(
    List<CompiHubPipelineVersion> pipelineVersions, String version
  ) {
    for (CompiHubPipelineVersion v : pipelineVersions) {
      if (v.getVersion().equals(version)) {
        return Optional.of(v);
      }
    }
    return Optional.empty();
  }
}
