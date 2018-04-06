package org.sing_group.compi.xmlio.entities;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "param")
public class Parameter implements Cloneable {

	private String name;
	private String shortName;
	private String description;
	
	public Parameter() {
	}

	/**
	 * Getter of the name attribute
	 * 
	 * @return The value of the name attribute
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Changes the value of the name attribute
	 * 
	 * @param name
	 *            Global variable
	 */
	public void setName(final String name) {
		this.name = name.replaceAll(" ", "");
	}
	
	/**
	 * Getter of the shortName attribute
	 * 
	 * @return The value of the shortName attribute
	 */
	@XmlAttribute
	public String getShortName() {
		return shortName;
	}

	/**
	 * Changes the value of the shortName attribute
	 * 
	 * @param shortName
	 *            Global variable
	 */
	public void setShortName(final String shortName) {
		this.shortName = shortName.replaceAll(" ", "");
	}
	
	/**
	 * Getter of the description attribute
	 * 
	 * @return The value of the description attribute
	 */
	@XmlAttribute
	public String getDescription() {
		return description;
	}

	/**
	 * Changes the value of the description attribute
	 * 
	 * @param description
	 *            Global variable
	 */
	public void setDescription(final String description) {
		this.description = description;
	}


}
