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
package org.sing_group.compi.dk.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sing_group.compi.dk.cli.MapBuilder;
import org.sing_group.compi.dk.cli.TemplateProcessor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PipelineDockerFile {
  private static final Logger logger = Logger.getLogger(PipelineDockerFile.class.getName());
  private static final String COMPI_URL_TEMPLATE =
    "https://maven.sing-group.org/repository/maven-releases/org/sing_group/compi-cli/%version/compi-cli-%version.tar.gz";
  private static final String COMPI_URL_TEMPLATE_SNAPSHOTS =
    "https://maven.sing-group.org/repository/maven-snapshots/org/sing_group/compi-cli/%version-SNAPSHOT/compi-cli-%version-%timestamp-%build.tar.gz";
  public static final String DEFAULT_BASE_IMAGE = "ubuntu:16.04";
  public static String DEFAULT_COMPI_VERSION;
  public static final String IMAGE_FILES_DIR = "image-files";
  private static final String VERSIONS_FILENAME = "versions";

  private File dockerFile;
  private File baseDirectory;

  private String baseImage = DEFAULT_BASE_IMAGE;
  private String compiVersion = DEFAULT_COMPI_VERSION;

  static {
    try {
      Properties p = new Properties();
      p.load(PipelineDockerFile.class.getResourceAsStream("/compi-dk.version"));
      DEFAULT_COMPI_VERSION = p.getProperty("compi-dk.version").toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public PipelineDockerFile(File directory) {
    this.baseDirectory = directory;
    this.dockerFile = new File(directory.toString() + File.separator + "Dockerfile");
  }

  public void setBaseImage(String baseImage) {
    this.baseImage = baseImage;
  }

  public void setCompiVersion(String compiVersion) {
    this.compiVersion = compiVersion;
  }

  public void downloadImageFilesIfNecessary() throws IOException, SAXException, ParserConfigurationException {
    createImageFilesDirIfNeccesary();
    // downloadJREIfNecessary();
    downloadCompiTarGzIfNecessary();
  }

  public void createDockerFile() throws IOException {
    // create file
    new TemplateProcessor().processTemplate(
      "Dockerfile.vm",
      MapBuilder.<String, String>newMapOf()
        .put("baseImage", baseImage)
        .put("maintainer", System.getProperty("user.name"))
        .build(),
      dockerFile
    );
  }

  public File getBaseDirectory() {
    return baseDirectory;
  }

  public File getDownloadedCompiTarGz() {
    File compiJar = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR + File.separator + "compi.tar.gz");
    if (compiJar.exists()) {
      return compiJar;
    } else {
      return null;
    }
  }

  public File getCompiJar() {
    File imagesDir = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR);
    File compiJar =
      findFile(
        (file) -> file.getName().startsWith("compi-cli") && file.getName().endsWith("jar"),
        imagesDir
      );

    if (compiJar == null) {
      throw new IllegalArgumentException("Compi jar not found in: " + imagesDir);
    }

    return compiJar;
  }

  private File findFile(Function<File, Boolean> filter, File directory) {
    for (File file : directory.listFiles()) {
      if (file.isDirectory()) {
        return findFile(filter, file);
      }
      if (filter.apply(file)) {
        return file;
      }
    }
    return null;
  }

  private void createImageFilesDirIfNeccesary() {
    File imageFilesDir = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR);
    if (!imageFilesDir.exists()) {
      imageFilesDir.mkdir();
    }
  }

  private void downloadCompiTarGzIfNecessary()
    throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
    String currentCompiVersion = getCurrentDownloadedCompiVersion();
    if (!this.compiVersion.equals(currentCompiVersion)) {
      downloadToFile(
        new URL(getCompiURL()), new File(baseDirectory.toString() + File.separator + IMAGE_FILES_DIR + File.separator + "compi.tar.gz")
      );
      uncompressTarGz(
        new File(baseDirectory.toString() + File.separator + IMAGE_FILES_DIR + File.separator + "compi.tar.gz")
      );
      updateDownloadedCompiVersion();
    }
  }

  private void uncompressTarGz(File file) throws FileNotFoundException, IOException {
    logger.info("Uncompressing: " + file);
    try (TarArchiveInputStream fin = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(file)))) {
      TarArchiveEntry entry;
      while ((entry = fin.getNextTarEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        File curfile = new File(file.getParent(), entry.getName());
        File parent = curfile.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
        IOUtils.copy(fin, new FileOutputStream(curfile));
      }
    }
  }

  private void updateDownloadedCompiVersion() throws FileNotFoundException, IOException {
    File versionsFile =
      new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR + File.separator + VERSIONS_FILENAME);
    Properties props = new Properties();
    if (versionsFile.exists()) {
      try (FileInputStream in = new FileInputStream(versionsFile)) {
        props.load(in);
      }
    }
    props.setProperty("compi", compiVersion);
    try (FileOutputStream out = new FileOutputStream(versionsFile)) {
      props.store(out, "");
    }
  }

  private String getCurrentDownloadedCompiVersion() throws IOException {
    File versionsFile =
      new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR + File.separator + VERSIONS_FILENAME);
    Properties props = new Properties();
    if (versionsFile.exists()) {
      try (FileInputStream in = new FileInputStream(versionsFile)) {
        props.load(in);
        return props.getProperty("compi");
      }
    } else {
      return null;
    }
  }

  private String getCompiURL() throws SAXException, IOException, ParserConfigurationException {
    String noSnapshotVersion = compiVersion;
    if (compiVersion.endsWith("-SNAPSHOT")) {
      noSnapshotVersion = compiVersion.replace("-SNAPSHOT", "");
      URL snapshotMetadata =
        new URL(getPathDir(COMPI_URL_TEMPLATE_SNAPSHOTS.replaceAll("%version", noSnapshotVersion))
          + "/maven-metadata.xml"
        );
      ByteArrayOutputStream metadataXMLbaos = new ByteArrayOutputStream();
      downloadTo(snapshotMetadata, metadataXMLbaos);
      String metadataXML = new String(metadataXMLbaos.toByteArray());
      DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = df.newDocumentBuilder();
      Document document = builder.parse(new ByteArrayInputStream(metadataXML.getBytes()));

      String timestamp = document.getElementsByTagName("timestamp").item(0).getTextContent();
      String buildNumber = document.getElementsByTagName("buildNumber").item(0).getTextContent();
      return COMPI_URL_TEMPLATE_SNAPSHOTS.replaceAll("%version", noSnapshotVersion).replaceAll("%timestamp", timestamp)
        .replaceAll("%build", buildNumber);
    }
    return COMPI_URL_TEMPLATE.replaceAll("%version", noSnapshotVersion);
  }

  private void downloadToFile(URL url, File f) throws IOException {
    try (OutputStream out = new FileOutputStream(f)) {
      downloadTo(url, out);
    }
  }

  private void downloadTo(URL url, OutputStream out) throws IOException {
    logger.info("Trying to download: " + url);
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      httpclient.execute(new HttpGet(url.toString()), (response) -> {
        if (response.getStatusLine().getStatusCode() != 200) {
          logger.severe("Cannot download. Status: " + response.getStatusLine());
          throw new RuntimeException("Cannot download " + url);
        }
        long length = response.getEntity().getContentLength();
        long total = 0;
        InputStream in = response.getEntity().getContent();
        byte[] data = new byte[1024];
        int readed = -1;
        while ((readed = in.read(data)) != -1) {
          out.write(data, 0, readed);
          total += readed;
          logger.fine(((float) total / (float) length) * 100 + "%");
        }
        return null;
      });
    }
  }

  private String getPathDir(String url) {
    return url.toString().substring(0, url.toString().lastIndexOf("/"));
  }
}
