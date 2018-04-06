package org.sing_group.compi.xmlio.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the {@link Foreach} tag obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
@XmlRootElement(name = "foreach")
public class Foreach {

	private String element;
	private String source;
	private String as;

	/**
	 * Getter of the element attribute
	 * 
	 * @return The value of the element attribute
	 */
	@XmlAttribute
	public String getElement() {
		return element;
	}

	/**
	 * Changes the value of the element attribute
	 * 
	 * @param element
	 *            Global variable
	 */
	public void setElement(final String element) {
		this.element = element;
	}

	/**
	 * Getter of the source attribute
	 * 
	 * @return The value of the source attribute
	 */
	@XmlAttribute
	public String getSource() {
		return source;
	}

	/**
	 * Changes the value of the source attribute
	 * 
	 * @param source
	 *            Global variable
	 */
	public void setSource(final String source) {
		this.source = source;
	}

	/**
	 * Getter of the as attribute
	 * 
	 * @return The value of the as attribute
	 */
	@XmlAttribute
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