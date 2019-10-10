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

	public Foreach(Pipeline pipeline) {
	  super(pipeline);
	}
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
