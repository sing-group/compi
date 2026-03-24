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
package org.sing_group.compi.dk.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.json.JSONObject;
import org.sing_group.compi.dk.CompiDKException;

/**
 * Implementation of CompiDkModule that loads module information from a ZIP file
 * downloaded from a URL.
 * 
 * @author hlfernandez
 *
 */
public class UrlModule implements CompiDkModule {
  private static final Logger LOGGER = Logger.getLogger(UrlModule.class.getName());

  private final String url;
  private final Path tempDir;
  private Path extractionBaseDir;
  private JSONObject metadata;
  private boolean extracted = false;

  public UrlModule(String url) throws IOException {
    this.url = url;
    this.tempDir = Files.createTempDirectory("compi-dk-module-");
    this.extractionBaseDir = this.tempDir;
    this.tempDir.toFile().deleteOnExit();
  }

  private void ensureExtracted() throws IOException {
    if (!extracted) {
      downloadAndExtract();
      resolveExtractionBaseDir();
      loadMetadata();
      extracted = true;
    }
  }

  private void downloadAndExtract() throws IOException {
    LOGGER.info("Downloading module from: " + url);

    try (
      InputStream in = URI.create(url).toURL().openStream();
      ZipArchiveInputStream zipIn = new ZipArchiveInputStream(in)
    ) {

      ZipArchiveEntry entry;
      while ((entry = zipIn.getNextZipEntry()) != null) {
        if (!entry.isDirectory()) {
          Path entryPath = tempDir.resolve(entry.getName());
          Files.createDirectories(entryPath.getParent());

          try (FileOutputStream out = new FileOutputStream(entryPath.toFile())) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zipIn.read(buffer)) > 0) {
              out.write(buffer, 0, len);
            }
          }
        }
      }
    }

    LOGGER.info("Module extracted to: " + tempDir);
  }

  private void resolveExtractionBaseDir() throws IOException {
    final List<Path> topLevelEntries;
    try (java.util.stream.Stream<Path> entries = Files.list(tempDir)) {
      topLevelEntries = entries.collect(Collectors.toList());
    }

    if (topLevelEntries.size() == 1 && Files.isDirectory(topLevelEntries.get(0))) {
      extractionBaseDir = topLevelEntries.get(0);
      LOGGER.info("Using module root directory: " + extractionBaseDir);
    } else {
      extractionBaseDir = tempDir;
      LOGGER.info("Using extraction root directory: " + extractionBaseDir);
    }
  }

  private void loadMetadata() throws IOException {
    File metadataFile = extractionBaseDir.resolve("metadata.json").toFile();
    if (!metadataFile.exists()) {
      throw new CompiDKException(
        "Missing metadata.json in module ZIP", "The module ZIP file must contain a metadata.json file", null
      );
    }

    try {
      String content = new String(Files.readAllBytes(metadataFile.toPath()));
      this.metadata = new JSONObject(content);
    } catch (Exception e) {
      throw new CompiDKException("Invalid metadata.json", "Error parsing metadata.json: " + e.getMessage(), e);
    }
  }

  @Override
  public String getName() {
    try {
      ensureExtracted();
      return metadata.optString("name", "");
    } catch (IOException e) {
      throw new CompiDKException("Error loading module", "Failed to load module metadata: " + e.getMessage(), e);
    }
  }

  @Override
  public String getDescription() {
    try {
      ensureExtracted();
      return metadata.optString("description", "");
    } catch (IOException e) {
      throw new CompiDKException("Error loading module", "Failed to load module metadata: " + e.getMessage(), e);
    }
  }

  @Override
  public String getVersion() {
    try {
      ensureExtracted();
      return metadata.optString("version", "");
    } catch (IOException e) {
      throw new CompiDKException("Error loading module", "Failed to load module metadata: " + e.getMessage(), e);
    }
  }

  @Override
  public String getTasks() throws IOException {
    ensureExtracted();
    return readFileContent("tasks.xml");
  }

  @Override
  public String getMetadata() throws IOException {
    ensureExtracted();
    return readFileContent("metadata.xml");
  }

  @Override
  public String getParams() throws IOException {
    ensureExtracted();
    return readFileContent("parameters.xml");
  }

  @Override
  public String getRunners() throws IOException {
    ensureExtracted();
    return readFileContent("runners.xml");
  }

  @Override
  public List<String> getFiles() throws IOException {
    ensureExtracted();

    List<String> allFiles = new ArrayList<>();

    // First, add files from the bundles folder (if it exists)
    Path bundlesDir = extractionBaseDir.resolve("bundles");
    if (Files.exists(bundlesDir) && Files.isDirectory(bundlesDir)) {
      LOGGER.info("Found bundles directory, processing bundle contents");

      // Check if there are any directories in bundles - if so, create a ZIP
      // preserving structure
      boolean hasDirectories =
        Files.list(bundlesDir)
          .anyMatch(path -> Files.isDirectory(path));

      if (hasDirectories) {
        LOGGER.info("Creating ZIP from bundles directory structure");
        String zipFileName = "bundles-content.zip";
        Path zipFile = tempDir.resolve("generated-bundles").resolve(zipFileName);
        Files.createDirectories(zipFile.getParent());

        // Create ZIP preserving the structure from bundles directory
        createZipFromBundlesDirectory(bundlesDir, zipFile);
        allFiles.add(zipFile.toString());
      }

      // Process individual ZIP files in bundles directory
      Files.list(bundlesDir)
        .filter(path -> Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".zip"))
        .forEach(zipFile -> {
          LOGGER.info("Adding bundle ZIP file: " + zipFile.getFileName());
          allFiles.add(zipFile.toString());
        });
    }

    // Then, download files from URLs listed in files.txt (if it exists)
    File filesFile = extractionBaseDir.resolve("files.txt").toFile();
    if (filesFile.exists()) {
      Path filesDir = tempDir.resolve("files");
      Files.createDirectories(filesDir);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filesFile)))) {
        String line;
        int fileIndex = 0;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty()) {
            String fileName = "module-file-" + fileIndex + ".zip";
            Path localFile = filesDir.resolve(fileName);

            LOGGER.info("Downloading file from: " + line);
            try (
              InputStream in = URI.create(line).toURL().openStream();
              FileOutputStream out = new FileOutputStream(localFile.toFile())
            ) {

              byte[] buffer = new byte[1024];
              int len;
              while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
              }
            }

            allFiles.add(localFile.toString());
            fileIndex++;
          }
        }
      }
    }

    return allFiles;
  }

  @Override
  public String getDockerfile() throws IOException {
    ensureExtracted();
    return readFileContent("Dockerfile");
  }

  private String readFileContent(String fileName) throws IOException {
    File file = extractionBaseDir.resolve(fileName).toFile();
    if (!file.exists()) {
      return "";
    }

    return new String(Files.readAllBytes(file.toPath()));
  }

  /**
   * Creates a ZIP file from the bundles directory structure, preserving the
   * directory structure.
   * 
   * @param bundlesDir
   *          the bundles directory
   * @param zipFile
   *          the target ZIP file path
   * @throws IOException
   *           if an I/O error occurs
   */
  private void createZipFromBundlesDirectory(Path bundlesDir, Path zipFile) throws IOException {
    try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(zipFile.toFile()))) {
      Files.walk(bundlesDir)
        .filter(path -> !Files.isDirectory(path))
        .forEach(path -> {
          try {
            // Get relative path from bundles directory
            Path relativePath = bundlesDir.relativize(path);
            ZipArchiveEntry entry = new ZipArchiveEntry(relativePath.toString());
            entry.setSize(Files.size(path));
            zipOut.putArchiveEntry(entry);

            Files.copy(path, zipOut);
            zipOut.closeArchiveEntry();
          } catch (IOException e) {
            throw new RuntimeException("Failed to add file to ZIP: " + path, e);
          }
        });
    }
  }

  /**
   * Creates a ZIP file from a directory structure.
   * 
   * @param sourceDir
   *          the source directory to zip
   * @param zipFile
   *          the target ZIP file path
   * @throws IOException
   *           if an I/O error occurs
   */
  private void createZipFromDirectory(Path sourceDir, Path zipFile) throws IOException {
    try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(new FileOutputStream(zipFile.toFile()))) {
      Files.walk(sourceDir)
        .filter(path -> !Files.isDirectory(path))
        .forEach(path -> {
          try {
            Path relativePath = sourceDir.relativize(path);
            ZipArchiveEntry entry = new ZipArchiveEntry(relativePath.toString());
            entry.setSize(Files.size(path));
            zipOut.putArchiveEntry(entry);

            Files.copy(path, zipOut);
            zipOut.closeArchiveEntry();
          } catch (IOException e) {
            throw new RuntimeException("Failed to add file to ZIP: " + path, e);
          }
        });
    }
  }

  /**
   * Cleanup temporary files. This should be called when the module is no longer
   * needed.
   */
  public void cleanup() {
    try {
      Files.walk(tempDir)
        .map(Path::toFile)
        .forEach(File::delete);
    } catch (IOException e) {
      LOGGER.warning("Failed to cleanup temporary directory: " + e.getMessage());
    }
  }
}