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
