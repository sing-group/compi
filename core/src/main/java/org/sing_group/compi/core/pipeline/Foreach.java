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
package org.sing_group.compi.core.pipeline;

/**
 * Represents the {@link Foreach} tag obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class Foreach extends Task {

	private String of;
	private String in;
	private String as;

	/**
	 * Getter of the element attribute
	 * 
	 * @return The value of the element attribute
	 */
	public String getOf() {
		return of;
	}

	/**
	 * Changes the value of the of attribute
	 * 
	 * @param of
	 *            Global variable
	 */
	public void setOf(final String of) {
		this.of = of;
	}

	/**
	 * Getter of the in attribute
	 * 
	 * @return The value of the in attribute
	 */
	public String getIn() {
		return in;
	}

	/**
	 * Changes the value of the in attribute
	 * 
	 * @param in
	 *            attribute
	 */
	public void setIn(final String in) {
		this.in = in;
	}

	/**
	 * Getter of the as attribute
	 * 
	 * @return The value of the as attribute
	 */
	public String getAs() {
		return as;
	}

	/**
	 * Changes the value of the as attribute
	 * 
	 * @param as
	 *            Global variable
	 */
	public void setAs(final String as) {
		this.as = as;
	}

}
