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

import static org.sing_group.compi.dk.cli.CommonParameters.DOCKER_REMOVE_DANGLING;
import static org.sing_group.compi.dk.cli.CommonParameters.DOCKER_REMOVE_DANGLING_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.DOCKER_REMOVE_DANGLING_LONG;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PROJECT_PATH_LONG;
import static org.sing_group.compi.dk.cli.CommonParameters.TAG_WITH_VERSION;
import static org.sing_group.compi.dk.cli.CommonParameters.TAG_WITH_VERSION_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.TAG_WITH_VERSION_LONG;

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

import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.dk.hub.CompiProjectDirectory;
import org.sing_group.compi.dk.project.PipelineDockerFile;
import org.sing_group.compi.dk.project.ProjectConfiguration;
import org.sing_group.compi.dk.project.PropertiesFileProjectConfiguration;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class BuildCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(BuildCommand.class.getName());

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
      getProjectPathOption(),
      getTagWithVersionOption(),
      getRemoveDockerDanglingOption()
    );
  }

  private Option<?> getProjectPathOption() {
    return new DefaultValuedStringOption(
      PROJECT_PATH_LONG, PROJECT_PATH, PROJECT_PATH_DESCRIPTION, PROJECT_PATH_DEFAULT_VALUE
    );
  }

  private Option<?> getTagWithVersionOption() {
    return new FlagOption(
      TAG_WITH_VERSION_LONG, TAG_WITH_VERSION, TAG_WITH_VERSION_DESCRIPTION
    );
  }

  private Option<?> getRemoveDockerDanglingOption() {
    return new FlagOption(
      DOCKER_REMOVE_DANGLING_LONG, DOCKER_REMOVE_DANGLING, DOCKER_REMOVE_DANGLING_DESCRIPTION
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {
    File directory = new File((String) parameters.getSingleValue(this.getOption(PROJECT_PATH)));
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

    String compiVersion = projectConfiguration.getCompiVersion();
    if (compiVersion == null) {
      compiVersion = PipelineDockerFile.DEFAULT_COMPI_VERSION;
    }

    String imageName = projectConfiguration.getImageName();
    if (imageName == null) {
      LOGGER.severe(
        "Image name not found in configuration file (" + PropertiesFileProjectConfiguration.IMAGE_NAME_PROPERTY
          + " property)"
      );
      System.exit(1);
    }

    File dockerFile = new File(directory + File.separator + "Dockerfile");

    if (!dockerFile.exists()) {
      LOGGER.severe(
        "Dockerfile does not exist: " +
          dockerFile
      );
      System.exit(1);
    }

    PipelineDockerFile pipelineDockerFile = new PipelineDockerFile(directory);
    pipelineDockerFile.setCompiVersion(compiVersion);
    pipelineDockerFile.downloadImageFilesIfNecessary();

    boolean isValid = validatePipeline(pipelineDockerFile);

    boolean tagDockerImageWithPipelineVersion = parameters.hasFlag(this.getOption(TAG_WITH_VERSION));
    boolean dockerRemoveDanglingImages = parameters.hasFlag(this.getOption(DOCKER_REMOVE_DANGLING));

    if (isValid) {
      buildDockerImage(directory, imageName, dockerFile, tagDockerImageWithPipelineVersion, dockerRemoveDanglingImages);
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
      return pipelineDockerFile.getCompiJar().toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void buildDockerImage(
    File directory, String imageName, File dockerFile, boolean tagDockerImageWithPipelineVersion,
    boolean dockerRemoveDanglingImages
  ) throws IOException, InterruptedException {
    LOGGER.info("Building docker image (dockerfile: " + dockerFile + ")");

    String versionTag = versionTag(directory, imageName, tagDockerImageWithPipelineVersion);

    Process p = Runtime.getRuntime().exec(new String[] {
      "/bin/bash", "-c", "docker build -t " + imageName + " " + versionTag + directory.getAbsolutePath()
    });
    Thread stdoutThreads = redirectOutputToLogger(p);

    int returnValue = p.waitFor();
    stdoutThreads.join();

    if (returnValue != 0) {
      LOGGER.severe("Docker build has returned a non-zero value: " + returnValue);
      System.exit(1);
    } else {
      LOGGER.info("Docker image has been built properly");
    }

    if (dockerRemoveDanglingImages) {
      Process p2 = Runtime.getRuntime().exec(new String[] {
        "/bin/bash", "-c",
        "IMAGES=$(docker images -f \"dangling=true\" -q); if [ ! -z \"$IMAGES\" ]; then docker rmi $IMAGES; fi"
      });

      int returnValue2 = p2.waitFor();

      if (returnValue2 != 0) {
        LOGGER.severe("Docker rmi command has returned a non-zero value: " + returnValue);
        System.exit(1);
      } else {
        LOGGER.info("Docker remove dangling images command succeeded");
      }
    }
  }

  private String versionTag(File directory, String imageName, boolean tagDockerImageWithPipelineVersion) {
    if (!tagDockerImageWithPipelineVersion) {
      return "";
    }

    CompiProjectDirectory compiProjectDir = new CompiProjectDirectory(directory);

    String version = null;
    try {
      version = compiProjectDir.getPipelineVersion();
    } catch (IllegalArgumentException | IOException | PipelineValidationException e1) {
      e1.printStackTrace();
      System.exit(1);
    }

    return "-t " + imageName + ":" + version + " ";
  }

  private Thread redirectOutputToLogger(Process p) {
    Thread stdoutThread = new Thread(() -> {
      try (Scanner sc = new Scanner(p.getInputStream())) {
        while (sc.hasNextLine()) {
          LOGGER.info("DOCKER BUILD: " + sc.nextLine());
        }
      }
    });
    stdoutThread.setName("Docker stdout");
    stdoutThread.start();

    Thread stderrThread = new Thread(() -> {
      try (Scanner sc = new Scanner(p.getErrorStream())) {
        while (sc.hasNextLine()) {
          LOGGER.severe("DOCKER BUILD ERROR: " + sc.nextLine());
        }
      }
    });
    stderrThread.setName("Docker stderr");
    stderrThread.start();

    Thread stdoutsThread = new Thread(() -> {
      try {
        stdoutThread.join();
        stderrThread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    stdoutsThread.start();
    return stdoutsThread;
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
        } catch (InvocationTargetException e) {
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
    private static class ExitTrappedException extends Error {}
  }
}
