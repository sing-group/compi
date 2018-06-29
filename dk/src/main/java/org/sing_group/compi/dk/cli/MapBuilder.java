package org.sing_group.compi.dk.cli;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
  private Map<K, V> map = new HashMap<>();

  public static <K, V> MapBuilder<K, V> newMapOf() {
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