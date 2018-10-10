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
package org.sing_group.compi.core.loops;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * Obtains the values of the task foreach tag when the element attribute
 * contains "var"
 * 
 * @author Hugo López Fernández
 *
 */
public class RangeLoopValuesGenerator extends AbstractLoopValuesGenerator {
  private final List<String> toExecute;

  public RangeLoopValuesGenerator(VariableResolver resolver, Foreach foreach) {
    super(resolver, foreach);
    this.toExecute = new LinkedList<>();
  }

  /**
   * Splits all the values in the task source tag
   * 
   * @param source Indicates the content of the task source tag
   */
  @Override
  protected List<String> getValuesFromResolvedSource(String source) {
    final String[] sourceStrings = source.split(":");

    if (sourceStrings.length != 2) {
      throw new IllegalArgumentException("Range source must have two numbers: <startInclusive:endInclusive>");
    }

    try {
      int startInclusive = valueOf(sourceStrings[0].trim());
      int endInclusive = valueOf(sourceStrings[1].trim());

      if(startInclusive > endInclusive) {
        throw new IllegalArgumentException("Range start must be equal or greater than range end");
      }
      this.toExecute.addAll(rangeClosed(startInclusive, endInclusive).mapToObj(Integer::toString).collect(toList()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Range source must have two numbers: startInclusive, endInclusive");
    }

    return this.toExecute;
  }
}
