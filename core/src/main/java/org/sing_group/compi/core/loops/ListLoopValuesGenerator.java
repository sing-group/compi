package org.sing_group.compi.core.loops;

import java.util.LinkedList;
import java.util.List;

import org.sing_group.compi.core.VariableResolver;

/**
 * Obtains the values of the task foreach tag when the element
 * attribute contains "var"
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ListLoopValuesGenerator extends AbstractLoopValuesGenerator {

	private final List<String> toExecute;

	public ListLoopValuesGenerator(VariableResolver resolver) {
	  super(resolver);
		this.toExecute = new LinkedList<>();
	}

	/**
	 * Splits all the values in the task source tag
	 * 
	 * @param source
	 *            Indicates the content of the task source tag
	 */
	@Override
  protected List<String> getValuesFromResolvedSource(String source) {
    final String[] sourceStrings = source.split(",");
    for (final String s : sourceStrings) {
      this.toExecute.add(s);
    }
    return this.toExecute;
  }
}
