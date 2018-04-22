package org.sing_group.compi.xmlio;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

/**
 * Methods for building {@link Pipeline} objects from files
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class PipelineParser {

	/**
	 * Reads a pipeline XML file and returns it as a {@link Pipeline} object
	 * 
	 * @param f
	 *            the XML input file
	 * @return the parsed Pipeline
	 * @throws JAXBException if a problem in XML parsing and validation occurs
	 */
	public static Pipeline parsePipeline(File f) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(Pipeline.class);
		final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Pipeline pipeline = (Pipeline) jaxbUnmarshaller.unmarshal(f);
		addTaskParameters(pipeline);
		return pipeline;
	}

	/**
	 * Obtain the content inside ${...} in the {@link Task} exec tag
	 * 
	 * @param pipeline
	 *            Contains all the {@link Task}
	 */
	private static void addTaskParameters(Pipeline pipeline) {
		final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

		for (final Task task : pipeline.getTasks()) {
			final Matcher matcher = pattern.matcher(task.getExec());
			while (matcher.find()) {
				task.addParameter(matcher.group(1));
			}
		}
	}

}
