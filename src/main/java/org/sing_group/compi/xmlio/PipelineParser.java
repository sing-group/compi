package org.sing_group.compi.xmlio;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;

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
		addProgramParameters(pipeline);
		return pipeline;
	}

	/**
	 * Obtain the content inside ${...} in the {@link Program} exec tag
	 * 
	 * @param programs
	 *            Contains all the {@link Program}
	 */
	private static void addProgramParameters(Pipeline pipeline) {
		final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");

		for (final Program program : pipeline.getPrograms()) {
			final Matcher matcher = pattern.matcher(program.getExec());
			while (matcher.find()) {
				program.addParameter(matcher.group(1));
			}
		}
	}

}
