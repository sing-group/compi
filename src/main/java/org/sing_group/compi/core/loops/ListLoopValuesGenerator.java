package org.sing_group.compi.core.loops;

import java.util.LinkedList;
import java.util.List;

/**
 * Obtains the values of the program foreach tag when the element
 * attribute contains "var"
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ListLoopValuesGenerator implements LoopValuesGenerator {

	private final List<String> toExecute;

	public ListLoopValuesGenerator() {
		this.toExecute = new LinkedList<>();
	}

	/**
	 * Splits all the values in the program source tag
	 * 
	 * @param source
	 *            Indicates the content of the program source tag
	 */
	@Override
	public List<String> getValues(final String source) {
		final String[] sourceStrings = source.split(",");
		for (final String s : sourceStrings) {
			this.toExecute.add(s);
		}
		return this.toExecute;
	}

}
