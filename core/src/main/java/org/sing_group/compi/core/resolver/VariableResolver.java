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
