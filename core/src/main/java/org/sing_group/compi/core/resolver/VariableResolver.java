package org.sing_group.compi.core.resolver;

import java.util.Set;

/**
 * A Variable resolver obtains the value of a variable given its name
 *
 * Created by lipido on 29/03/17.
 */
public interface VariableResolver {

	/**
	 * Get the content of the variable
	 *
	 * @param variable
	 *            Indicates the variable to resolve
	 * @return The content of the variable
	 * @throws IllegalArgumentException
	 *             If the variable does not exist
	 */
	String resolveVariable(String variable)
			throws IllegalArgumentException;

	/**
	 * Get all resolvable variables that this resolver can resolve
	 * @return The set of all resolvable variables
	 */
	Set<String> getVariableNames();
}
