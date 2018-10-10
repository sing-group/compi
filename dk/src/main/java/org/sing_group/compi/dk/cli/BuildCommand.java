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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.sing_group.compi.dk.project.PipelineDockerFile;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class BuildCommand extends AbstractCommand {
  // private static final Logger logger =
  // LogManager.getLogger(BuildCommand.class);
  private static final Logger logger = Logger.getLogger(BuildCommand.class.getName());

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
      logger.severe("Directory " + directory + " does not exist");
      System.exit(1);
    }

    File compiProjectFile = new File(directory + File.separator + ProjectConfiguration.COMPI_PROJECT_FILENAME);
    if (!compiProjectFile.exists()) {
      logger.severe("Compi project file does not exist: " + compiProjectFile);
      System.exit(1);
    }

    PropertiesFileProjectConfiguration projectConfiguration = new PropertiesFileProjectConfiguration(compiProjectFile);

    String compiVersion = projectConfiguration.getCompiVersion();
    if (compiVersion == null) {
      compiVersion = PipelineDockerFile.DEFAULT_COMPI_VERSION;
    }

    String imageName = projectConfiguration.getImageName();
    if (imageName == null) {
      logger.severe(
        "Image name not found in configuration file (" + PropertiesFileProjectConfiguration.IMAGE_NAME_PROPERTY
          + " property)"
        );
      System.exit(1);
    }

    File dockerFile = new File(directory + File.separator + "Dockerfile");

    if (!dockerFile.exists()) {
      logger.severe(
        "Dockerfile does not exist: " +
          dockerFile
        );
      System.exit(1);
    }

    PipelineDockerFile pipelineDockerFile = new PipelineDockerFile(directory);
    pipelineDockerFile.setCompiVersion(compiVersion);
    pipelineDockerFile.downloadImageFilesIfNecessary();

    boolean isValid = validatePipeline(pipelineDockerFile);

    // build image
    if (isValid) {
      buildDockerImage(directory, imageName, dockerFile);
    }
  }

  private boolean validatePipeline(
    PipelineDockerFile pipelineDockerFile
    )
    throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {

    try {
      MainMethodRunner mainRunner =
        new MainMethodRunner("org.sing_group.compi.cli.CompiCLI", getDownloadedCompiJarURL(pipelineDockerFile));

      int returnValue = mainRunner.run(new String[] {
        "validate", "-p", pipelineDockerFile.getBaseDirectory() + File.separator + "pipeline.xml"
      });

      if (returnValue != 0) {
        return false;
      } else {
        return true;
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }

  }

  private URL getDownloadedCompiJarURL(PipelineDockerFile pipelineDockerFile) {
    try {
      return pipelineDockerFile.getDownloadedCompiJar().toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void buildDockerImage(
    File directory, String imageName, File dockerFile
    )
    throws IOException, InterruptedException {
    logger.info("Building docker image (dockerfile: " + dockerFile + ")");
    Process p = Runtime.getRuntime().exec(new String[] {
      "/bin/bash", "-c", "docker build -t " + imageName + " " + directory.getAbsolutePath()
    });
    redirectOutputToLogger(p);

    int returnValue = p.waitFor();
    if (returnValue != 0) {
      logger.severe("Docker build has returned a non-zero value: " + returnValue);
      System.exit(1);
    }

    // logger.info("Docker image built: " + imageName);
  }

  private void redirectOutputToLogger(Process p) {
    Thread stdoutThread = new Thread(() -> {
      try (Scanner sc = new Scanner(p.getInputStream())) {
        while (sc.hasNextLine()) {
          logger.info("DOCKER BUILD: " + sc.nextLine());
        }
      }
    });
    stdoutThread.setName("Docker stdout");
    stdoutThread.start();

    Thread stderrThread = new Thread(() -> {
      try (Scanner sc = new Scanner(p.getErrorStream())) {
        while (sc.hasNextLine()) {
          logger.severe("DOCKER BUILD ERROR: " + sc.nextLine());
        }
      }
    });
    stderrThread.setName("Docker stderr");
    stderrThread.start();
  }

  private static class MainMethodRunner {
    private PrintStream oldStdErr;
    private int returnStatus = Integer.MIN_VALUE;
    private URL jarFile;
    private String className;

    public MainMethodRunner(String className, URL jarFile) {
      this.className = className;
      this.jarFile = jarFile;
    }

    public int run(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
      ClassNotFoundException, NoSuchMethodException, SecurityException {
      try (URLClassLoader loader = new URLClassLoader(new URL[] {
        this.jarFile
      })) {

        Class<?> clazz = loader.loadClass(this.className);
        Method mainMethod = clazz.getMethod("main", String[].class);
        forbidSystemExitCall();
        try {
          mainMethod.invoke(null, new Object[] {
            args
          });
        } catch (ExitTrappedException e) {
          restoreStdErr();
        }

        enableSystemExitCall();
        return this.returnStatus;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void silenceStdErr() {
      oldStdErr = System.err;
      System.setErr(new PrintStream(new ByteArrayOutputStream()));
    }

    private void restoreStdErr() {
      System.setErr(oldStdErr);
    }

    private void forbidSystemExitCall() {
      final SecurityManager securityManager = new SecurityManager() {
        public void checkPermission(Permission permission) {
          if (permission.getName().startsWith("exitVM")) {
            returnStatus = Integer.parseInt(permission.getName().substring(permission.getName().lastIndexOf('.') + 1));
            silenceStdErr();
            throw new ExitTrappedException();
          }
        }
      };
      System.setSecurityManager(securityManager);
    }

    private static void enableSystemExitCall() {
      System.setSecurityManager(null);
    }

    @SuppressWarnings("serial")
    private static class ExitTrappedException extends SecurityException {}
  }
}
