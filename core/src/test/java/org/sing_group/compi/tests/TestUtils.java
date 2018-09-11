package org.sing_group.compi.tests;

import java.util.HashMap;
import java.util.Map;

import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;

public class TestUtils {

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
}
