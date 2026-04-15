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
package org.sing_group.compi.dk.cli;

import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_LONG;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;
import static org.sing_group.compi.dk.cli.CommonParameters.RUNNERS_FILE;
import static org.sing_group.compi.dk.cli.CommonParameters.RUNNERS_FILE_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.RUNNERS_FILE_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.RUNNERS_FILE_LONG;
import static org.sing_group.compi.dk.cli.CommonParameters.URL;
import static org.sing_group.compi.dk.cli.CommonParameters.URL_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.URL_LONG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.sing_group.compi.dk.module.UrlModule;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class AddModuleCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(AddModuleCommand.class.getName());

  @Override
  public String getName() {
    return "add-module";
  }

  @Override
  public String getDescriptiveName() {
    return "Add module to project";
  }

  @Override
  public String getDescription() {
    return "Downloads a module from a ZIP URL and adds it to the project";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getProjectPathOption(),
      getPipelineFileOption(),
      getRunnersFileOption(),
      getUrlOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, PROJECT_PATH_DESCRIPTION + " (default: .)", PROJECT_PATH_DEFAULT_VALUE
    );
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE, PIPELINE_FILE_DESCRIPTION + " (default: pipeline.xml)",
      PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  private Option<?> getRunnersFileOption() {
    return new DefaultValuedStringOption(
      RUNNERS_FILE_LONG, RUNNERS_FILE, RUNNERS_FILE_DESCRIPTION + " (default: runners.xml)", RUNNERS_FILE_DEFAULT_VALUE
    );
  }

  private Option<?> getUrlOption() {
    return new StringOption(URL_LONG, URL, URL_DESCRIPTION, false, true);
  }

  @Override
  public void execute(Parameters parameters) throws Exception {
    String projectPath = parameters.getSingleValueString(getOption(PROJECT_PATH));
    String pipelineFile = parameters.getSingleValueString(getOption(PIPELINE_FILE));
    String runnersFile = parameters.getSingleValueString(getOption(RUNNERS_FILE));
    String url = parameters.getSingleValueString(getOption(URL));

    File projectDirectory = new File(projectPath);
    LOGGER.info("Adding module to project: " + projectDirectory.getAbsolutePath());

    if (!projectDirectory.exists()) {
      LOGGER.severe("Project directory does not exist: " + projectDirectory.getAbsolutePath());
      System.exit(1);
    }

    File compiProjectFile = new File(projectDirectory, ProjectConfiguration.COMPI_PROJECT_FILENAME);
    if (!compiProjectFile.exists()) {
      LOGGER.severe("Compi project file does not exist: " + compiProjectFile.getAbsolutePath());
      System.exit(1);
    }

    // Load the module
    UrlModule module = null;
    try {
      LOGGER.info("Loading module from URL: " + url);
      module = new UrlModule(url);

      String moduleName = module.getName();
      String moduleVersion = module.getVersion();

      if (moduleName.isEmpty()) {
        LOGGER.severe("Module name is empty or missing in metadata.json");
        System.exit(1);
      }

      if (moduleVersion.isEmpty()) {
        LOGGER.severe("Module version is empty or missing in metadata.json");
        System.exit(1);
      }

      LOGGER.info("Adding module: " + moduleName + " version " + moduleVersion);

      // 1. Update compi.project file
      PropertiesFileProjectConfiguration projectConfig = new PropertiesFileProjectConfiguration(compiProjectFile);
      projectConfig.addModule(moduleName, moduleVersion);
      projectConfig.save();
      LOGGER.info("Updated project configuration with module: " + moduleName + ":" + moduleVersion);

      // 2. Update Dockerfile
      String dockerfileContent = module.getDockerfile();
      if (!dockerfileContent.isEmpty()) {
        File dockerFile = new File(projectDirectory, "Dockerfile");
        if (dockerFile.exists()) {
          String currentContent = new String(Files.readAllBytes(dockerFile.toPath()));
          String updatedContent = currentContent + "\n# Module: " + moduleName + "\n" + dockerfileContent + "\n";
          Files.write(dockerFile.toPath(), updatedContent.getBytes());
          LOGGER.info("Updated Dockerfile with module content");
        } else {
          LOGGER.warning("Dockerfile not found in project directory");
        }
      }

      // 3. Update pipeline.xml
      String tasksContent = module.getTasks();
      String metadataContent = module.getMetadata();
      String paramsContent = module.getParams();
      updateXmlFile(
        new File(projectDirectory, pipelineFile), tasksContent, metadataContent, paramsContent, "pipeline", moduleName
      );

      // 4. Update runners.xml
      String runnersContent = module.getRunners();
      File runnersFileObj = new File(projectDirectory, runnersFile);
      if (!runnersContent.isEmpty() && runnersFileObj.exists()) {
        updateRunnersFile(runnersFileObj, runnersContent, moduleName);
      }

      // 5. Extract module files
      List<String> moduleFiles = module.getFiles();
      for (String zipFile : moduleFiles) {
        extractZipFile(zipFile, projectDirectory);
      }

      // 6. Write license file
      String licenseContent = module.getLicense();
      if (!licenseContent.isEmpty()) {
        String sanitizedModuleName = moduleName.replaceAll("[^a-zA-Z0-9_.\\-]", "_");
        File licenseFile = new File(projectDirectory, "LICENSE_" + sanitizedModuleName);
        Files.write(licenseFile.toPath(), licenseContent.getBytes());
        LOGGER.info("Written license file: " + licenseFile.getAbsolutePath());
      } else {
        LOGGER.warning("No LICENSE file found in module: " + moduleName);
      }

      LOGGER.info("Module " + moduleName + " added successfully");

    } catch (Exception e) {
      LOGGER.severe("Error adding module: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    } finally {
      if (module != null) {
        module.cleanup();
      }
    }
  }

  private void updateXmlFile(
    File xmlFile, String tasksContent, String metadataContent, String paramsContent, String fileType, String moduleName
  ) throws IOException {
    if (!xmlFile.exists()) {
      LOGGER.warning(fileType + " file not found: " + xmlFile.getAbsolutePath());
      return;
    }

    String content = new String(Files.readAllBytes(xmlFile.toPath()));

    // Insert tasks content
    if (!tasksContent.isEmpty()) {
      String tasksMarker = "</tasks>";
      if (content.contains(tasksMarker)) {
        String beforeTasks = content.substring(0, content.lastIndexOf(tasksMarker));
        String afterTasks = content.substring(content.lastIndexOf(tasksMarker));
        content = beforeTasks + "\n\t<!-- Module: " + moduleName + " -->\n" + tasksContent + "\n\t" + afterTasks;
      }
    }

    // Insert params content
    if (!paramsContent.isEmpty()) {
      String paramsMarker = "</params>";
      if (content.contains(paramsMarker)) {
        String beforeParams = content.substring(0, content.lastIndexOf(paramsMarker));
        String afterParams = content.substring(content.lastIndexOf(paramsMarker));
        content = beforeParams + "\n\t<!-- Module: " + moduleName + " -->\n" + paramsContent + "\n\t" + afterParams;
      }
    }

    // Insert metadata content
    if (!metadataContent.isEmpty()) {
      String metadataMarker = "</metadata>";
      if (content.contains(metadataMarker)) {
        String beforeMetadata = content.substring(0, content.lastIndexOf(metadataMarker));
        String afterMetadata = content.substring(content.lastIndexOf(metadataMarker));
        content =
          beforeMetadata + "\n\t<!-- Module: " + moduleName + " -->\n" + metadataContent + "\n\t" + afterMetadata;
      }
    }

    Files.write(xmlFile.toPath(), content.getBytes());
    LOGGER.info("Updated " + fileType + " file with module content");
  }

  private void updateRunnersFile(File runnersFile, String runnersContent, String moduleName) throws IOException {
    String content = new String(Files.readAllBytes(runnersFile.toPath()));

    String runnersMarker = "</runners>";
    if (content.contains(runnersMarker)) {
      String beforeRunners = content.substring(0, content.lastIndexOf(runnersMarker));
      String afterRunners = content.substring(content.lastIndexOf(runnersMarker));
      content = beforeRunners + "\n\t<!-- Module: " + moduleName + " -->\n" + runnersContent + "\n\t" + afterRunners;
      Files.write(runnersFile.toPath(), content.getBytes());
      LOGGER.info("Updated runners file with module content");
    }
  }

  private void extractZipFile(String zipFilePath, File targetDirectory) throws IOException {
    LOGGER.info("Extracting module file: " + zipFilePath);

    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          Path entryPath = Paths.get(targetDirectory.getAbsolutePath(), entry.getName());
          Files.createDirectories(entryPath.getParent());

          try (FileOutputStream out = new FileOutputStream(entryPath.toFile())) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zipIn.read(buffer)) > 0) {
              out.write(buffer, 0, len);
            }
          }
        }
        zipIn.closeEntry();
      }
    }

    LOGGER.info("Extracted module file to: " + targetDirectory.getAbsolutePath());
  }
}