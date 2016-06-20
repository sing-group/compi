package es.uvigo.esei.compi.xmlio;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.uvigo.esei.compi.core.loops.LoopProgram;
import es.uvigo.esei.compi.xmlio.entities.Program;

/**
 * Obtains the command to execute
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class Resolver {

	private final String xmlParamsFile;

	/**
	 * 
	 * @param xmlParamsPath
	 *            Indicates the path of the params file
	 */
	public Resolver(final String xmlParamsPath) {
		this.xmlParamsFile = xmlParamsPath;
	}

	/**
	 * Replace the exec tag in {@link Program} to obtain the command to execute
	 * with the strings in the params file
	 * 
	 * @param program
	 *            Indicates the {@link Program}
	 * @param tag
	 *            Indicates the exec tag to parse
	 * @throws IllegalArgumentException
	 *             If the tag does not exist
	 * @throws ParserConfigurationException
	 *             If there is a configuration error
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 * 
	 */
	public void resolveToExecute(final Program program, final String tag)
			throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		final String tagParsed = resolveStringInParamsFile(tag);
		program.setToExecute(program.getToExecute().replace("${" + tag + "}", tagParsed));
	}

	/**
	 * Replace the exec tag in {@link Program} with loops to obtain the command
	 * to execute
	 * 
	 * @param program
	 *            Indicates the {@link Program}
	 * @param loopProgram
	 *            Indicates the {@link LoopProgram}
	 * @throws IllegalArgumentException
	 *             If the tag does not exist
	 * @throws ParserConfigurationException
	 *             If there is a configuration error
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 */
	public void resolveForEach(final Program program, final LoopProgram loopProgram)
			throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		for (final String tag : program.getExecStrings()) {
			if (loopProgram.getAs().equals(tag)) {
				loopProgram.setToExecute(loopProgram.getToExecute().replace("${" + tag + "}", loopProgram.getSource()));
			} else {
				final String parsed = resolveStringInParamsFile(tag);
				loopProgram.setToExecute(loopProgram.getToExecute().replace("${" + tag + "}", parsed));
			}
		}
	}

	/**
	 * 
	 * Get the content of the tag in the params file
	 * 
	 * @param tag
	 *            Indicates the tag to find in the params file
	 * @return The content of the tag in the params file
	 * @throws IllegalArgumentException
	 *             If the tag does not exist
	 * @throws ParserConfigurationException
	 *             If there is a configuration error
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 */
	private String resolveStringInParamsFile(final String tag)
			throws IllegalArgumentException, ParserConfigurationException, IOException, SAXException {
		String resolvedString = new String();
		if (this.xmlParamsFile == null) {
			throw new IllegalArgumentException("Params file is missing, the tag \"" + tag + "\" can't be replaced");
		}
		final File xmlFile = new File(this.xmlParamsFile);
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		final Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();

		final NodeList nodeList = doc.getElementsByTagName(tag);
		if (nodeList.getLength() == 0) {
			throw new IllegalArgumentException(
					"The tag: \"" + tag + "\" doesn't exist in the file " + this.xmlParamsFile);
		}

		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				final Element element = (Element) node;
				resolvedString = element.getTextContent();
			}
		}
		return resolvedString;
	}

}
