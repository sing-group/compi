/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.pipeline.Pipeline;

public class CompiProjectDirectory {
  private static final Logger LOGGER = Logger.getLogger(CompiProjectDirectory.class.getName());
  
  public static final String DEFAULT_FILE_PIPELINE = "pipeline.xml";
  public static final String DEFAULT_FILE_README = "README.md";
  public static final String DEFAULT_FILE_DEPENDENCIES = "DEPENDENCIES.md";
  
  public static final String DEFAULT_FILE_DOCKERFILE = "Dockerfile";
  public static final String DEFAULT_FILE_LICENSE = "LICENSE";
  public static final String DEFAULT_FILE_HUB_METADATA = "hub.metadata";
  
  public static final String DEFAULT_DIRECTORY_RUNNERS = "runners-examples";
  public static final String DEFAULT_DIRECTORY_PARAMS = "params-examples";

  private File directory;
  private String pipelineFileName = DEFAULT_FILE_PIPELINE;
  private String readmeFileName = DEFAULT_FILE_README;
  private String dependenciesFileName = DEFAULT_FILE_DEPENDENCIES;
  private String dockerfileFileName = DEFAULT_FILE_DOCKERFILE;
  private String licenseFileName = DEFAULT_FILE_LICENSE;
  private String hubMetadataFileName = DEFAULT_FILE_HUB_METADATA;
  private String runnersDirectoryName = DEFAULT_DIRECTORY_RUNNERS;
  private String paramsDirectoryName = DEFAULT_DIRECTORY_PARAMS;

  public CompiProjectDirectory(File directory) {
    this.directory = directory;
  }

  public String getPipelineVersion() throws IllegalArgumentException, IOException, PipelineValidationException {
    return Pipeline.fromFile(getPipelineFile()).getVersion();
  }

  private File getPipelineFile() {
    File pipelineFile = projectFile(this.pipelineFileName);
    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException(
        "The pipeline file is required but " + this.pipelineFileName + " does not exist in the project directory"
      );
    }
    return pipelineFile;
  }

  public void toZipFile(File destZipFile) throws FileNotFoundException, IOException {
    try (
      FileOutputStream fos = new FileOutputStream(destZipFile);
      ZipOutputStream zos = new ZipOutputStream(fos);
    ) {
      zipFile(zos, getPipelineFile(), DEFAULT_FILE_PIPELINE);

      checkAndZipFile(zos, this.readmeFileName, DEFAULT_FILE_README);
      checkAndZipFile(zos, this.dependenciesFileName, DEFAULT_FILE_DEPENDENCIES);
      checkAndZipFile(zos, this.dockerfileFileName, DEFAULT_FILE_DOCKERFILE);
      checkAndZipFile(zos, this.licenseFileName, DEFAULT_FILE_LICENSE);
      checkAndZipFile(zos, this.hubMetadataFileName, DEFAULT_FILE_HUB_METADATA);

      checkAndZipFile(zos, this.runnersDirectoryName, DEFAULT_DIRECTORY_RUNNERS);
      checkAndZipFile(zos, this.paramsDirectoryName, DEFAULT_DIRECTORY_PARAMS);
    }
  }
  
  private void checkAndZipFile(ZipOutputStream zos, String fileName, String zipFileName)
    throws FileNotFoundException, IOException {
    File file = projectFile(fileName);
    if (file.exists()) {
      LOGGER.info("Found " + zipFileName + ": " + file.getAbsolutePath());
      zipFile(zos, file, zipFileName);
    } else {
      LOGGER.info("Missing " + zipFileName + ": " + file.getAbsolutePath());
    }
  }

  private File projectFile(String fileName) {
    return new File(this.directory, fileName);
  }
  
  private static void zipFile(ZipOutputStream zos, File fileToZip, String fileName) throws FileNotFoundException, IOException {
    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.closeEntry();
      } else {
        zos.putNextEntry(new ZipEntry(fileName + "/"));
        zos.closeEntry();
      }
      File[] children = fileToZip.listFiles();
      for (File childFile : children) {
        zipFile(zos, childFile, fileName + "/" + childFile.getName());
      }
    } else {
      try (FileInputStream fis = new FileInputStream(fileToZip)) {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zos.write(bytes, 0, length);
        }
        fis.close();
      }
    }
  }
}
