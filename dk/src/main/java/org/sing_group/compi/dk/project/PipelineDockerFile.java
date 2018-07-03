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
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sing_group.compi.dk.cli.MapBuilder;
import org.sing_group.compi.dk.cli.TemplateProcessor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PipelineDockerFile {
  private static final Logger logger = Logger.getLogger( PipelineDockerFile.class.getName() );
  private static final String JRE_URL =
    "https://maven.sing-group.org/repository/maven-releases/com/oracle/java/jre-mod.java.base-mod.java.logging-mod.java.xml-mod.java.naming/10.0.1/jre-mod.java.base-mod.java.logging-mod.java.xml-mod.java.naming-10.0.1.tgz";
  private static final String COMPI_URL_TEMPLATE =
    "https://maven.sing-group.org/repository/maven-releases/org/sing_group/compi-cli/%version/compi-cli-%version-jar-with-dependencies.jar";

  private static final String COMPI_URL_TEMPLATE_SNAPSHOTS =
    "https://maven.sing-group.org/repository/maven-snapshots/org/sing_group/compi-cli/%version-SNAPSHOT/compi-cli-%version-%timestamp-%build-jar-with-dependencies.jar";
  public static final String DEFAULT_BASE_IMAGE = "ubuntu:16.04";
  public static final String DEFAULT_COMPI_VERSION = "1.1-SNAPSHOT";
  public static final String IMAGE_FILES_DIR = "image-files";
  private static final String VERSIONS_FILENAME = "versions";

  private File dockerFile;
  private File baseDirectory;

  private String baseImage = DEFAULT_BASE_IMAGE;
  private String compiVersion = DEFAULT_COMPI_VERSION;

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
    downloadJREIfNecessary();
    downloadCompiJarIfNecessary();
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

  public File getDownloadedCompiJar() {
    File compiJar = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR + File.separator + "compi.jar");
    if (compiJar.exists()) {
      return compiJar;
    } else {
      return null;
    }
  }

  private void createImageFilesDirIfNeccesary() {
    File imageFilesDir = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR);
    if (!imageFilesDir.exists()) {
      imageFilesDir.mkdir();
    }
  }

  private void downloadJREIfNecessary() throws MalformedURLException, IOException {
    File jreFile = new File(this.baseDirectory + File.separator + IMAGE_FILES_DIR + File.separator + "jre.tgz");
    if (!jreFile.exists()) {
      downloadToFile(new URL(JRE_URL), jreFile);
    }

  }

  private void downloadCompiJarIfNecessary()
    throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
    String currentCompiVersion = getCurrentDownloadedCompiVersion();
    if (!this.compiVersion.equals(currentCompiVersion)) {
      downloadToFile(
        new URL(getCompiURL()), new File(baseDirectory.toString() + File.separator + IMAGE_FILES_DIR + File.separator + "compi.jar")
      );
      updateDownloadedCompiVersion();
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
