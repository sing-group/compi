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

	private List<Task> tasks = new LinkedList<>();
	private List<ParameterDescription> parameterDescriptions = new LinkedList<>();

	public Pipeline() {
	}

	/**
	 * 
	 * @param tasks
	 *            Indicates all the {@link Task} in the {@link Pipeline}
	 */
	public Pipeline(final List<Task> tasks) {
		this.tasks = tasks;
	}

	/**
	 * 
	 * @param tasks
	 *            Indicates the list of tasks of the Pipeline
	 * @param params
	 *            Indicates all the {@link Task} and the
	 *            {@link ParameterDescription} in the {@link Pipeline}
	 */
	public Pipeline(final List<Task> tasks, final List<ParameterDescription> params) {
		this.tasks = tasks;
		this.parameterDescriptions = params;
	}

	/**
	 * Getter of the tasks attribute
	 * 
	 * @return The value of the tasks attribute
	 */
	@XmlElementWrapper(name = "tasks")
	@XmlElement(name = "task")
	public List<Task> getTasks() {
		return tasks;
	}

	/**
	 * Changes the value of the tasks attribute
	 * 
	 * @param tasks attribute
	 */
	public void setTasks(final List<Task> tasks) {
		this.tasks = tasks;
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
	 * Returns all parameter names in the pipeline, organized by each task id
	 * 
	 * @return a map of task ids to a list of parameter names
	 */
	public Map<String, List<String>> getParametersByTask() {
		return this.tasks.stream().collect(toMap(t -> t.getId(), t -> t.getParameters()));
	}

	/**
	 * Returns a mapping of configurable parameters to tasks using them
	 * 
	 * @return a mapping of configurable parameters to tasks using them
	 */
	public Map<String, List<Task>> getTasksByParameter() {
		return this.getAllParameters().stream().collect(toMap(t -> t,
				p -> this.tasks.stream().filter(task -> task.getParameters().contains(p)).collect(toList())));
	}

	private Set<String> getAllParameters() {
		return this.tasks.stream()
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
