/*-
 * #%L
 * Compi Core
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
        if (!line.startsWith("#") && line.indexOf("=") != -1) {
          variables.put(line.substring(0, line.indexOf("=")), line.substring(line.indexOf("=") + 1));
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
