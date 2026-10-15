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

import static org.sing_group.compi.dk.CompiDKVersion.getCompiDKVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class GetCompiSkillsCommand extends AbstractCommand {
  private static final Logger LOGGER = Logger.getLogger(GetCompiSkillsCommand.class.getName());

  private static final String VERSIONS_TSV_URL =
    "https://raw.githubusercontent.com/sing-group/compi-pipeline-engineering/master/versions.tsv";
  private static final String RELEASE_ZIP_URL_TEMPLATE =
    "https://github.com/sing-group/compi-pipeline-engineering/archive/refs/tags/v%s.zip";
  private static final String SKILL_MD_FILENAME = "SKILL.md";

  private static final String PATH = "p";
  private static final String PATH_LONG = "path";
  private static final String PATH_DESCRIPTION = "path where the SKILL.md file will be downloaded";

  @Override
  public String getName() {
    return "get-compi-skills";
  }

  @Override
  public String getDescriptiveName() {
    return "Get Compi pipeline engineering skills";
  }

  @Override
  public String getDescription() {
    return "Downloads the SKILL.md file for the current compi version from the compi-pipeline-engineering repository";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      new StringOption(PATH_LONG, PATH, PATH_DESCRIPTION, false, true)
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {
    String path = parameters.getSingleValueString(getOption(PATH));
    String compiVersion = getCompiDKVersion();

    LOGGER.info("Current compi version: " + compiVersion);

    // Create target directory if it does not exist
    File targetDir = new File(path);
    if (!targetDir.exists()) {
      LOGGER.info("Creating directory: " + targetDir.getAbsolutePath());
      if (!targetDir.mkdirs()) {
        LOGGER.severe("Could not create directory: " + targetDir.getAbsolutePath());
        System.exit(1);
      }
    }

    // 1. Download and parse versions.tsv
    Map<String, String> versionMapping = downloadAndParseVersions();
    if (versionMapping.isEmpty()) {
      LOGGER.severe("versions.tsv is empty or could not be parsed");
      System.exit(1);
    }

    // 2. Resolve the skills version for the current compi version
    String skillsVersion = findSkillsVersion(compiVersion, versionMapping);
    if (skillsVersion == null) {
      LOGGER.severe("No compatible skills version found for compi version: " + compiVersion);
      System.exit(1);
    }

    LOGGER.info("Using skills version: " + skillsVersion);

    // 3. Download the release ZIP and extract SKILL.md
    String zipUrl = String.format(RELEASE_ZIP_URL_TEMPLATE, skillsVersion);
    LOGGER.info("Downloading skills from: " + zipUrl);
    downloadAndExtractSkillMd(zipUrl, targetDir);

    LOGGER.info("SKILL.md successfully written to: " + targetDir.getAbsolutePath());
  }

  private Map<String, String> downloadAndParseVersions() throws IOException {
    Map<String, String> mapping = new LinkedHashMap<>();
    try (
      InputStream in = URI.create(VERSIONS_TSV_URL).toURL().openStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in))
    ) {
      String line;
      boolean firstLine = true;
      while ((line = reader.readLine()) != null) {
        if (firstLine) {
          firstLine = false; // skip header
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length >= 2) {
          mapping.put(parts[0].trim(), parts[1].trim());
        }
      }
    }
    return mapping;
  }

  private String findSkillsVersion(String compiVersion, Map<String, String> versionMapping) {
    // 1. Exact match on raw version string
    if (versionMapping.containsKey(compiVersion)) {
      LOGGER.info("Exact version match found for: " + compiVersion);
      return versionMapping.get(compiVersion);
    }

    // 2. If SNAPSHOT, strip suffix and retry exact match
    String baseVersion = compiVersion;
    if (compiVersion.endsWith("-SNAPSHOT")) {
      baseVersion = compiVersion.replace("-SNAPSHOT", "");
      if (versionMapping.containsKey(baseVersion)) {
        LOGGER.info("Exact version match found for base version: " + baseVersion);
        return versionMapping.get(baseVersion);
      }
    }

    // 3. Fallback: find the highest compi_version in the map that is <=
    // baseVersion
    LOGGER.info("No exact match for version " + baseVersion + ", searching for the latest compatible version");
    int[] baseParts = parseVersion(baseVersion);
    if (baseParts == null) {
      LOGGER.warning("Could not parse current compi version: " + baseVersion);
      return null;
    }

    String bestMapVersion = null;
    int[] bestParts = null;

    for (String mapVersion : versionMapping.keySet()) {
      int[] parts = parseVersion(mapVersion);
      if (parts == null) {
        continue;
      }
      if (isLessOrEqual(parts, baseParts)) {
        if (bestParts == null || isGreater(parts, bestParts)) {
          bestMapVersion = mapVersion;
          bestParts = parts;
        }
      }
    }

    if (bestMapVersion != null) {
      LOGGER.info("Using latest compatible version: " + bestMapVersion);
      return versionMapping.get(bestMapVersion);
    }

    return null;
  }

  private int[] parseVersion(String version) {
    try {
      String[] parts = version.split("\\.");
      if (parts.length != 3) {
        return null;
      }
      return new int[] {
        Integer.parseInt(parts[0].trim()),
        Integer.parseInt(parts[1].trim()),
        Integer.parseInt(parts[2].trim())
      };
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private boolean isLessOrEqual(int[] a, int[] b) {
    if (a[0] != b[0])
      return a[0] < b[0];
    if (a[1] != b[1])
      return a[1] < b[1];
    return a[2] <= b[2];
  }

  private boolean isGreater(int[] a, int[] b) {
    if (a[0] != b[0])
      return a[0] > b[0];
    if (a[1] != b[1])
      return a[1] > b[1];
    return a[2] > b[2];
  }

  private void downloadAndExtractSkillMd(String zipUrl, File targetDir) throws IOException {
    try (
      InputStream in = URI.create(zipUrl).toURL().openStream();
      ZipInputStream zipIn = new ZipInputStream(in)
    ) {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        String entryName = entry.getName();
        String fileName = new File(entryName).getName();
        if (!entry.isDirectory() && fileName.equals(SKILL_MD_FILENAME)) {
          File outputFile = new File(targetDir, SKILL_MD_FILENAME);
          try (FileOutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zipIn.read(buffer)) > 0) {
              out.write(buffer, 0, len);
            }
          }
          LOGGER.info("Extracted " + SKILL_MD_FILENAME + " to: " + outputFile.getAbsolutePath());
          zipIn.closeEntry();
          return;
        }
        zipIn.closeEntry();
      }
    }
    LOGGER.severe(SKILL_MD_FILENAME + " not found in the downloaded release ZIP");
    System.exit(1);
  }
}
