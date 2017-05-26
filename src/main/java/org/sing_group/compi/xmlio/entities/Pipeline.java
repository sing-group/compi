package org.sing_group.compi.xmlio.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the {@link Pipeline} obtained in the XML pipeline file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
@XmlRootElement(name = "pipeline")
public class Pipeline {

	private List<Program> programs = new LinkedList<>();
	private List<Parameter> params = new LinkedList<>();

	public Pipeline() {
	}

	/**
	 * 
	 * @param programs
	 *            Indicates all the {@link Program} in the {@link Pipeline}
	 */
	public Pipeline(final List<Program> programs) {
		this.programs = programs;
	}
	
	/**
	 * 
	 * @param programs
	 * @param params
	 *            Indicates all the {@link Program} and the {@link Parameter} in the {@link Pipeline}
	 */
	public Pipeline(final List<Program> programs, final List<Parameter> params) {
		this.programs = programs;
		this.params = params;
	}

	/**
	 * Getter of the programs global variable
	 * 
	 * @return The value of the programs global variable
	 */
	@XmlElementWrapper(name = "programs")
	@XmlElement(name = "program")
	public List<Program> getPrograms() {
		return programs;
	}

	/**
	 * Changes the value of the programs global variable
	 * 
	 * @param programs
	 *            Global variable
	 */
	public void setPrograms(final List<Program> programs) {
		this.programs = programs;
	}
	
	/**
	 * Getter of the params global variable
	 * 
	 * @return The value of the params global variable
	 */
	@XmlElementWrapper(name = "params")
	@XmlElement(name = "param")
	public List<Parameter> getParams() {
		return params;
	}

	/**
	 * Changes the value of the params global variable
	 * 
	 * @param params
	 *            Global variable
	 */
	public void setParams(List<Parameter> params) {
		this.params = params;
	}

}
