/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * A {@code VariableResolver} implementation that reads variable names and values from a plain text file. Each line
 * assigns a variable name to a value separating them with a "=" character.
 *
 * @author Hugo López-Fernández
 */
public class ParamsFileVariableResolver implements VariableResolver {

  private File paramsFile;

  private MapVariableResolver mapResolver;

  /**
   * Creates a new {@code ParamsFileVariableResolver} instance for the specified parameters file.
   * 
   * @param paramsFile the path of the parameters file
   */
  public ParamsFileVariableResolver(final File paramsFile) {
    if (paramsFile != null) {
      this.paramsFile = paramsFile;
      if (!this.paramsFile.exists()) {
        throw new IllegalArgumentException("Params file " + this.paramsFile.toString() + " does not exist");
      }
      this.mapResolver = new MapVariableResolver(parsePlainText());
    } else {
      this.mapResolver = new MapVariableResolver();
    }
  }

  private Map<String, String> parsePlainText() {
    final Map<String, String> variables = new HashMap<>();

    try {
      Files.lines(this.paramsFile.toPath()).forEach(line -> {
        if (!line.startsWith("#")) {
          if (line.indexOf("=") != -1) {
            variables.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1));
          } else {
            variables.put(line.trim(), "yes");
          }
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return variables;
  }

  @Override
  public String resolveVariable(String variable) throws IllegalArgumentException {
    return this.mapResolver.resolveVariable(variable);
  }

  @Override
  public Set<String> getVariableNames() {
    return this.mapResolver.getVariableNames();
  }
}
