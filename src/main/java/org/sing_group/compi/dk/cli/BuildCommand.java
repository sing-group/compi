package org.sing_group.compi.dk.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sing_group.compi.dk.project.PipelineDockerFile;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class BuildCommand extends AbstractCommand {
  private static final Logger logger = LogManager.getLogger(BuildCommand.class);

  public String getName() {
    return "build";
  }

  public String getDescriptiveName() {
    return "Builds a compi project";
  }

  public String getDescription() {
    return "Builds a compi project";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      new DefaultValuedStringOption("path", "p", "path the new project to build", ".")
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {
    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
    logger.info("Building project in directory: " + directory);

    if (!directory.exists()) {
      logger.error("Directory " + directory + " does not exist");
      System.exit(1);
    }

    File compiProjectFile = new File(directory + File.separator + ProjectConfiguration.COMPI_PROJECT_FILENAME);
    if (!compiProjectFile.exists()) {
      logger.error("Compi project file does not exist: " + compiProjectFile);
      System.exit(1);
    }

    PropertiesFileProjectConfiguration projectConfiguration = new PropertiesFileProjectConfiguration(compiProjectFile);

    String compiVersion = projectConfiguration.getCompiVersion();
    if (compiVersion == null) {
      compiVersion = ProjectConfiguration.DEFAULT_COMPI_VERSION;
    }

    String imageName = projectConfiguration.getImageName();
    if (imageName == null) {
      logger.error(
        "Image name not found in configuration file (" + PropertiesFileProjectConfiguration.IMAGE_NAME_PROPERTY
          + " property)"
      );
      System.exit(1);
    }

    File dockerFile = new File(directory + File.separator + "Dockerfile");
    if (!dockerFile.exists()) {
      logger.error("Dockerfile does not exist: " + dockerFile);
      System.exit(1);
    }

    PipelineDockerFile pipelineDockerFile = new PipelineDockerFile(directory);
    pipelineDockerFile.setCompiVersion(compiVersion);
    pipelineDockerFile.writeOrUpdate();

    // TODO: validate pipeline

    // build image
    logger.info("Building docker image (dockerfile: " + dockerFile + ")");
    Process p = Runtime.getRuntime().exec(new String[] {
      "/bin/sh", "-c", "docker build -t "+ imageName+" "+directory.getAbsolutePath()
    });

    new Thread(() -> {
      try (Scanner sc = new Scanner(p.getInputStream())) {
        while (sc.hasNextLine()) {
          logger.info("DOCKER BUILD: " + sc.nextLine());
        }
      }
    }).start();

    new Thread(() -> {
      try (Scanner sc = new Scanner(p.getErrorStream())) {
        while (sc.hasNextLine()) {
          logger.error("DOCKER BUILD ERROR: " + sc.nextLine());
        }
      }
    }).start();

    int returnValue = p.waitFor();
    if (returnValue != 0) {
      logger.error("Docker build has returned a non-zero value: "+returnValue);
      System.exit(1);
    }
    
    logger.info("Docker image built: "+imageName);
  }
}
