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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Default adapter implementation of CompiDkModule that provides empty
 * implementations for all methods. This can be used as a base class for custom
 * implementations.
 * 
 * @author hlfernandez
 *
 */
public class CompiDkModuleAdapter implements CompiDkModule {

  @Override
  public String getName() {
    return "";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getVersion() {
    return "";
  }

  @Override
  public String getTasks() throws IOException {
    return "";
  }

  @Override
  public String getMetadata() throws IOException {
    return "";
  }

  @Override
  public String getParams() throws IOException {
    return "";
  }

  @Override
  public String getRunners() throws IOException {
    return "";
  }

  @Override
  public List<String> getFiles() throws IOException {
    return Collections.emptyList();
  }

  @Override
  public String getDockerfile() throws IOException {
    return "";
  }

  @Override
  public String getLicense() throws IOException {
    return "";
  }
}