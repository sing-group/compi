/*-
 * #%L
 * Compi Development Kit
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
