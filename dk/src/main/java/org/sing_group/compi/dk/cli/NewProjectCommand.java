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

import static java.lang.System.getProperty;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;
import static org.sing_group.compi.dk.project.ProjectConfiguration.COMPI_PROJECT_FILENAME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.dk.project.PipelineDockerFile;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class NewProjectCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(NewProjectCommand.class.getName());

  private static final String[] INITIAL_FILES = {
    "README.md", "DEPENDENCIES.md", "entrypoint.sh",
    "LICENSE", "DATASETS.md", "hub.config",
    "runners-examples/README.md",
    "runners-examples/generic-docker-runner.xml",
    "runners-examples/generic-slurm-runner.xml",
    "params-examples/README.md",
    "params-examples/params-1"
  };

  public String getName() {
    return "new-project";
  }

  public String getDescriptiveName() {
    return "Creates a new project";
  }

  public String getDescription() {
    return "Creates a new compi project";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getProjectPathOption(),
      getImageNameOption(),
      getBaseImageOption(),
      getcompiVersionOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, "path to create the compi-dk project", PROJECT_PATH_DEFAULT_VALUE
    );
  }

  private StringOption getImageNameOption() {
    return new StringOption("image-name", "n", "name for the docker image", false, true);
  }

  private DefaultValuedStringOption getBaseImageOption() {
    return new DefaultValuedStringOption(
      "base-image", "i", "base image for the docker image", PipelineDockerFile.DEFAULT_BASE_IMAGE
    );
  }

  private DefaultValuedStringOption getcompiVersionOption() {
    return new DefaultValuedStringOption(
      "compi-version", "v", "compi version", PipelineDockerFile.DEFAULT_COMPI_VERSION
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {

    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
    LOGGER.info("Creating project with path: " + parameters.getSingleValueString(getOption("p")));

    if (directory.exists()) {
      LOGGER.severe("Directory " + directory + " already exists");
      System.exit(1);
    }

    directory.mkdirs();

    String compiVersion = parameters.getSingleValueString(getOption("v"));
    String imageName = parameters.getSingleValueString(getOption("n"));

    PropertiesFileProjectConfiguration configuration =
      new PropertiesFileProjectConfiguration(new File(directory + File.separator + COMPI_PROJECT_FILENAME));

    configuration.setCompiVersion(compiVersion);
    configuration.setImageName(imageName);

    configuration.save();

    createDockerFile(
      directory, parameters.getSingleValueString(getOption("i")), getProperty("user.name"), compiVersion
    );

    createPipelineFile(directory);

    createGitIgnoreFile(directory);

    createCompiHubFiles(directory);
  }

  private void createGitIgnoreFile(File directory) throws FileNotFoundException {
    try (PrintStream out = new PrintStream(new FileOutputStream(directory + File.separator + ".gitignore"))) {
      out.println("/" + PipelineDockerFile.IMAGE_FILES_DIR + "/");
    }
  }

  private void createPipelineFile(File destDirectory) throws IOException {
    new TemplateProcessor().processTemplate(
      "pipeline.xml.vm",
      MapBuilder.<String, String>newMapOf().build(),
      new File(destDirectory.toString() + File.separator + "pipeline.xml")
    );
  }

  private void createDockerFile(File destDirectory, String baseImage, String maintainer, String compiVersion)
    throws IOException, SAXException, ParserConfigurationException {
    PipelineDockerFile dockerFile = new PipelineDockerFile(destDirectory);
    dockerFile.setBaseImage(baseImage);
    dockerFile.setCompiVersion(compiVersion);
    dockerFile.createDockerFile();
  }

  private void createCompiHubFiles(File directory) {
    Stream.of(INITIAL_FILES).forEach(f -> {
      copyToFile(this.getClass().getResourceAsStream(f), new File(directory, f));
    });
  }

  private static void copyToFile(InputStream inputStream, File destFile) {
    destFile.getAbsoluteFile().getParentFile().mkdirs();

    try (OutputStream output = new FileOutputStream(destFile)) {
      byte[] buffer = new byte[inputStream.available()];
      inputStream.read(buffer);
      output.write(buffer);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
