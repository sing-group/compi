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
package org.sing_group.compi.tests;

import java.util.HashMap;
import java.util.Map;

import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;

public class TestUtils {

  public static VariableResolver emptyResolver() {
    Map<String, String> variables = new HashMap<>();
    return new MapVariableResolver(variables);
  }
  
  public static VariableResolver resolverFor(String key, String value) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    return new MapVariableResolver(variables);
  }
  
  public static VariableResolver resolverFor(String key, String value, String key2, String value2) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    variables.put(key2, value2);
    return new MapVariableResolver(variables);
  }
  
  public static VariableResolver resolverFor(String key, String value, String key2, String value2, String key3, String value3) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    variables.put(key2, value2);
    variables.put(key3, value3);
    return new MapVariableResolver(variables);
  }
  
  public static VariableResolver resolverFor(String key, String value, String key2, String value2, String key3, String value3, String key4, String value4) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    variables.put(key2, value2);
    variables.put(key3, value3);
    variables.put(key4, value4);
    return new MapVariableResolver(variables);
  }
}
