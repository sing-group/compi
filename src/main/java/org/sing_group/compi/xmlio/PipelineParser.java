package org.sing_group.compi.xmlio;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;

/**
 * Obtains the content of the {@link Program} exec tag
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class PipelineParser {

	private static Pattern pattern;
	private static Matcher matcher;

	/**
	 * Obtain the content inside ${...} in the {@link Program} exec tag
	 * 
	 * @param programs
	 *            Contains all the {@link Program}
	 */
	public static void solveExec(final List<Program> programs) {
		for (final Program program : programs) {
			pattern = Pattern.compile("\\{(.*?)\\}");
			matcher = pattern.matcher(program.getExec());
			while (matcher.find()) {
				program.getExecStrings().add(matcher.group(1));
			}
		}
	}

	public static Pipeline parsePipeline(File f) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(Pipeline.class);
		final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (Pipeline) jaxbUnmarshaller.unmarshal(f);
	}

}
