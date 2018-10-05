package org.sing_group.compi.xmlio.entities;

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