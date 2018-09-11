package org.sing_group.compi.core.loops;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;

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
