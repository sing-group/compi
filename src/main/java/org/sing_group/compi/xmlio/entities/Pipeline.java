package org.sing_group.compi.xmlio.entities;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private List<ParameterDescription> parameterDescriptions = new LinkedList<>();

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
	 *            Indicates the list of programs of the Pipeline
	 * @param params
	 *            Indicates all the {@link Program} and the
	 *            {@link ParameterDescription} in the {@link Pipeline}
	 */
	public Pipeline(final List<Program> programs, final List<ParameterDescription> params) {
		this.programs = programs;
		this.parameterDescriptions = params;
	}

	/**
	 * Getter of the programs attribute
	 * 
	 * @return The value of the programs attribute
	 */
	@XmlElementWrapper(name = "programs")
	@XmlElement(name = "program")
	public List<Program> getPrograms() {
		return programs;
	}

	/**
	 * Changes the value of the programs attribute
	 * 
	 * @param programs
	 *            Global variable
	 */
	public void setPrograms(final List<Program> programs) {
		this.programs = programs;
	}

	/**
	 * Getter of the params attribute
	 * 
	 * @return The value of the params attribute
	 */
	@XmlElementWrapper(name = "params")
	@XmlElement(name = "param")
	public List<ParameterDescription> getParameterDescriptions() {
		return parameterDescriptions;
	}

	/**
	 * Changes the value of the parameterDescriptions attribute
	 * 
	 * @param parameterDescriptions
	 *            Global variable
	 */
	public void setParameterDescriptions(List<ParameterDescription> parameterDescriptions) {
		this.parameterDescriptions = parameterDescriptions;
	}

	/**
	 * Returns all parameter names in the pipeline, organized by each program id
	 * 
	 * @return a map of program ids to a list of parameter names
	 */
	public Map<String, List<String>> getParametersByProgram() {
		return this.programs.stream().collect(toMap(p -> p.getId(), p -> p.getParameters()));
	}

	/**
	 * Returns a mapping of configurable parameters to programs using them
	 * 
	 * @return a mapping of configurable parameters to programs using them
	 */
	public Map<String, List<Program>> getProgramsByParameter() {
		return this.getAllParameters().stream().collect(
				toMap(p -> p, p -> this.programs.stream().filter(program -> program.getParameters().contains(p))
						.collect(toList())));
	}

	private Set<String> getAllParameters() {
		return this.programs.stream()
				.map(p -> p.getParameters().stream()
						.filter(param -> p.getForeach() == null || !p.getForeach().getAs().equals(param))
						.collect(toList()))
				.flatMap(x -> x.stream()).collect(toSet());
	}

	/**
	 * Returns the {@link ParameterDescription} for a given a parameter name
	 * 
	 * @param parameterName
	 *            the parameter name
	 * @return the parameter description, null if it is not available
	 */
	public ParameterDescription getParameterDescription(String parameterName) {
		return this.getParameterDescriptions().stream().collect(toMap(p -> p.getName(), p -> p)).get(parameterName);
	}

}
