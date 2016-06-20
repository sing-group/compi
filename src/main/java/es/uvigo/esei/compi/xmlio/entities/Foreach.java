package es.uvigo.esei.compi.xmlio.entities;

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
	 * Getter of the element global variable
	 * 
	 * @return The value of the element global variable
	 */
	@XmlAttribute
	public String getElement() {
		return element;
	}

	/**
	 * Changes the value of the element global variable
	 * 
	 * @param element
	 *            Global variable
	 */
	public void setElement(final String element) {
		this.element = element;
	}

	/**
	 * Getter of the source global variable
	 * 
	 * @return The value of the source global variable
	 */
	@XmlAttribute
	public String getSource() {
		return source;
	}

	/**
	 * Changes the value of the source global variable
	 * 
	 * @param source
	 *            Global variable
	 */
	public void setSource(final String source) {
		this.source = source;
	}

	/**
	 * Getter of the as global variable
	 * 
	 * @return The value of the as global variable
	 */
	@XmlAttribute
	public String getAs() {
		return as;
	}

	/**
	 * Changes the value of the as global variable
	 * 
	 * @param as
	 *            Global variable
	 */
	public void setAs(final String as) {
		this.as = as;
	}

}