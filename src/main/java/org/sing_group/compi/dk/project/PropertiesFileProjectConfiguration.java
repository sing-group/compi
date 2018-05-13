package org.sing_group.compi.dk.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFileProjectConfiguration implements ProjectConfiguration {

  public static final String IMAGE_NAME_PROPERTY = "image.name";
  public static final String COMPI_VERSION_PROPERTY = "compi.version";

  private Properties prop = new Properties();
  private File configFile;

  public PropertiesFileProjectConfiguration(File configFile) {
    this.configFile = configFile;
    if (configFile.exists()) {
      read();
    }
  }


  @Override
  public String getImageName() {
    return prop.getProperty(IMAGE_NAME_PROPERTY);
  }

  @Override
  public String getCompiVersion() {
    return prop.getProperty(COMPI_VERSION_PROPERTY);
  }

  public void setImageName(String imageName) {
    prop.setProperty(IMAGE_NAME_PROPERTY, imageName);
  }

  public void setCompiVersion(String compiVersion) {
    prop.setProperty(COMPI_VERSION_PROPERTY, compiVersion);
  }

  public void save() {
    try (FileOutputStream out = new FileOutputStream(configFile)) {
      prop.store(out, "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void read() {
    try (FileInputStream in = new FileInputStream(this.configFile)) {
      this.prop = new Properties();
      this.prop.load(in);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
