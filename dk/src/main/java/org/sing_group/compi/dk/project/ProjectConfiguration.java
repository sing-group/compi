package org.sing_group.compi.dk.project;

public interface ProjectConfiguration {
  public static final String COMPI_PROJECT_FILENAME = "compi.project";
  public static final String DEFAULT_COMPI_VERSION = "1.1-SNAPSHOT";
  public String getImageName();
  public String getCompiVersion();
}
