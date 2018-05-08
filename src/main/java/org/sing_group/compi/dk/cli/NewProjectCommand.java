package org.sing_group.compi.dk.cli;

import static java.lang.System.getProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class NewProjectCommand extends AbstractCommand {
  private static final Logger logger = LogManager.getLogger(NewProjectCommand.class);

  private static final String JRE_URL =
    "https://maven.sing-group.org/repository/alfresco/com/oracle/java/jre/1.8.0_131/jre-1.8.0_131-linux.tgz";
  private static final String DEFAULT_BASE_IMAGE = "ubuntu:16.04";
  private static final String DEFAULT_COMPI_VERSION = "1.1-SNAPSHOT";
  private static final String COMPI_URL_TEMPLATE =
    "https://maven.sing-group.org/repository/maven-releases/org/sing_group/compi/%version/compi-%version-jar-with-dependencies.jar";
  private static final String COMPI_URL_TEMPLATE_SNAPSHOTS =
    "https://maven.sing-group.org/repository/maven-snapshots/org/sing_group/compi/%version-SNAPSHOT/compi-%version-%timestamp-%build-jar-with-dependencies.jar";

  


  public String getName() {
    return "new-project";
  }

  public String getDescriptiveName() {
    return "Creates a new project";
  }

  public String getDescription() {
    return "Creates a new compi project";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      new StringOption("path", "p", "path of the new project", false, true),
      new DefaultValuedStringOption("base-image", "i", "base image for the docker image", DEFAULT_BASE_IMAGE),
      new DefaultValuedStringOption("compi-version", "v", "compi version", DEFAULT_COMPI_VERSION)
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {

    File directory = new File((String) parameters.getSingleValue(this.getOption("p")));
    logger.info("Creating project with path: " + parameters.getSingleValueString(getOption("p")));

    if (directory.exists()) {
      logger.error("Directory " + directory + " already exists");
      System.exit(1);
    }

    directory.mkdirs();

    File jreDestination = new File(directory + File.separator + "jre.tgz");
    logger.info("Downloading JRE to " + jreDestination);
    downloadToFile(new URL(JRE_URL), jreDestination);
    logger.info("JRE downloaded");

    String compiVersion = parameters.getSingleValueString(getOption("v"));

    File compiDestination = new File(directory + File.separator + "compi.jar");
    logger.info("Downloading Compi to " + compiDestination);
    downloadToFile(new URL(getCompiURL(compiVersion)), compiDestination);
    logger.info("Compi downloaded");

    createDockerFile(directory, parameters.getSingleValueString(getOption("i")), getProperty("user.name"));

    createPipelineFile(directory);
  }

  private void createPipelineFile(File destDirectory) throws IOException {
    processTemplate(
      "pipeline.xml.vm",
      MapBuilder.<String, String>newMapOf().build(),
      new File(destDirectory.toString() + File.separator + "pipeline.xml")
    );
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

  private String getPathDir(String url) {
    return url.toString().substring(0, url.toString().lastIndexOf("/"));
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

  private void createDockerFile(File destDirectory, String baseImage, String maintainer) throws IOException {
    processTemplate(
      "Dockerfile.vm",
      MapBuilder.<String, String>newMapOf()
        .put("baseImage", baseImage)
        .put("maintainer", maintainer)
        .build(),
      new File(destDirectory.toString() + File.separator + "Dockerfile")
    );
  }

  private void processTemplate(String templateName, Map<String, String> variables, File destFile) throws IOException {
    Template dockerFileTemplate = getVelocityTemplate(templateName);
    VelocityContext context = new VelocityContext();
    variables.forEach((k, v) -> {
      context.put(k, v);
    });

    try (Writer out = new OutputStreamWriter(new FileOutputStream(destFile))) {
      dockerFileTemplate.merge(context, out);
    } catch (FileNotFoundException e) {
      // this should no happen, since the file must not exist because we have
      // created
      // the directory
      e.printStackTrace();
    }
  }

  private Template getVelocityTemplate(String templateName) {
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty("resource.loader", "class");
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
    Template dockerFileTemplate =
      ve.getTemplate(
        File.separator
          + getClass().getPackage().getName().replace('.', File.separatorChar) + File.separator + templateName
        );
        return dockerFileTemplate;
  }

  private static class MapBuilder<K, V> {
    private Map<K, V> map = new HashMap<>();

    private static <K, V> MapBuilder<K, V> newMapOf() {
      return new MapBuilder<K, V>();
    }

    public MapBuilder<K, V> put(K k, V v) {
      map.put(k, v);
      return this;
    }

    public Map<K, V> build() {
      return this.map;
    }
  }
}
