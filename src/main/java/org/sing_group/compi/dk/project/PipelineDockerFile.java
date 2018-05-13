package org.sing_group.compi.dk.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sing_group.compi.dk.cli.MapBuilder;
import org.sing_group.compi.dk.cli.TemplateProcessor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PipelineDockerFile {
  private static final Logger logger = LogManager.getLogger(PipelineDockerFile.class);
  private static final String JRE_URL =
    "https://maven.sing-group.org/repository/alfresco/com/oracle/java/jre/1.8.0_131/jre-1.8.0_131-linux.tgz";

  private static final String COMPI_URL_TEMPLATE =
    "https://maven.sing-group.org/repository/maven-releases/org/sing_group/compi/%version/compi-%version-jar-with-dependencies.jar";

  private static final String COMPI_URL_TEMPLATE_SNAPSHOTS =
    "https://maven.sing-group.org/repository/maven-snapshots/org/sing_group/compi/%version-SNAPSHOT/compi-%version-%timestamp-%build-jar-with-dependencies.jar";
  public static final String DEFAULT_BASE_IMAGE = "ubuntu:16.04";
  public static final String DEFAULT_COMPI_VERSION = "1.1-SNAPSHOT";
  private File dockerFile;

  private String baseImage = DEFAULT_BASE_IMAGE;
  private String compiVersion = DEFAULT_COMPI_VERSION;

  public PipelineDockerFile(File directory) {
    this.dockerFile = new File(directory.toString() + File.separator + "Dockerfile");
  }

  public void setBaseImage(String baseImage) {
    this.baseImage = baseImage;
  }

  public void setCompiVersion(String compiVersion) {
    this.compiVersion = compiVersion;
  }

  public void writeOrUpdate() throws IOException, SAXException, ParserConfigurationException {
    if (!dockerFile.exists()) {
      // create file
      new TemplateProcessor().processTemplate(
        "Dockerfile.vm",
        MapBuilder.<String, String>newMapOf()
          .put("baseImage", baseImage)
          .put("maintainer", System.getProperty("user.name"))
          .put("compiURL", getCompiURL(compiVersion))
          .put("jreURL", JRE_URL)
          .build(),
        dockerFile
      );
    } else {
      // update
      String dockerFileContents = new String(Files.readAllBytes(dockerFile.toPath()));
      dockerFileContents = updateAddCompiLine(dockerFileContents);
      try(OutputStream out = new FileOutputStream(dockerFile)) {
        out.write(dockerFileContents.getBytes());
      }
    }
  }

  private String updateAddCompiLine(String dockerFileContents)
    throws SAXException, IOException, ParserConfigurationException {
    Pattern p = Pattern.compile("ADD\\s+(.*)\\s+compi.jar");
    Matcher m = p.matcher(dockerFileContents);
    if (m.find()) {
      String compiURL = this.getCompiURL(compiVersion);
      if (!m.group(1).equals(compiURL)) {
        return m.replaceFirst("ADD " + compiURL + " compi.jar");
      } else {
        return dockerFileContents;
      }
    }
    throw new IllegalArgumentException("ADD <url> compi.jar line not found");
  }

  private String getCompiURL(String compiVersion) throws SAXException, IOException, ParserConfigurationException {
    if (compiVersion.endsWith("-SNAPSHOT")) {
      compiVersion = compiVersion.replace("-SNAPSHOT", "");
      URL snapshotMetadata =
        new URL(getPathDir(COMPI_URL_TEMPLATE_SNAPSHOTS.replaceAll("%version", compiVersion))
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
      return COMPI_URL_TEMPLATE_SNAPSHOTS.replaceAll("%version", compiVersion).replaceAll("%timestamp", timestamp)
        .replaceAll("%build", buildNumber);
    }
    return COMPI_URL_TEMPLATE.replaceAll("%version", compiVersion);
  }

  private void downloadTo(URL url, OutputStream out) throws IOException {
    logger.info("Trying to download: " + url);
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      httpclient.execute(new HttpGet(url.toString()), (response) -> {
        if (response.getStatusLine().getStatusCode() != 200) {
          logger.error("Cannot download. Status: " + response.getStatusLine());
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
          logger.debug(((float) total / (float) length) * 100 + "%");
        }
        return null;
      });
    }
  }

  private String getPathDir(String url) {
    return url.toString().substring(0, url.toString().lastIndexOf("/"));
  }

}
