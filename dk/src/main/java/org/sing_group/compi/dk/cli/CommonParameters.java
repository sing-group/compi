/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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

/**
 * This class provides names and descriptions for parameters shared across different commands.
 * 
 * @author hlfernandez
 *
 */
public class CommonParameters {
  public static final String PROJECT_PATH = "p";
  public static final String PROJECT_PATH_LONG = "path";
  public static final String PROJECT_PATH_DESCRIPTION = "path of the project";
  public static final String PROJECT_PATH_DEFAULT_VALUE = ".";
  
  public static final String PIPELINE_FILE = "p";
  public static final String PIPELINE_FILE_LONG = "pipeline";
  public static final String PIPELINE_FILE_DESCRIPTION = "XML pipeline file";
  public static final String PIPELINE_FILE_DEFAULT_VALUE = "pipeline.xml";

  public static final String TAG_WITH_VERSION = "tv";
  public static final String TAG_WITH_VERSION_LONG = "tag-version";
  public static final String TAG_WITH_VERSION_DESCRIPTION = "tag the Docker image with the pipeline version";
  
  public static final String DOCKER_REMOVE_DANGLING = "drd";
  public static final String DOCKER_REMOVE_DANGLING_LONG = "docker-remove-dangling";
  public static final String DOCKER_REMOVE_DANGLING_DESCRIPTION = "removes the Docker dangling images after building the Compi image";
}
