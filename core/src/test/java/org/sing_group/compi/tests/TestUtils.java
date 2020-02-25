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

  public static VariableResolver resolverFor(
    String key, String value, String key2, String value2, String key3, String value3
  ) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    variables.put(key2, value2);
    variables.put(key3, value3);
    return new MapVariableResolver(variables);
  }

  public static VariableResolver resolverFor(
    String key, String value, String key2, String value2, String key3, String value3, String key4, String value4
  ) {
    Map<String, String> variables = new HashMap<>();
    variables.put(key, value);
    variables.put(key2, value2);
    variables.put(key3, value3);
    variables.put(key4, value4);
    return new MapVariableResolver(variables);
  }
}
