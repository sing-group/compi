package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.VariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Obtains the command to execute
 *
 * @author Jesus Alvarez Casanova
 */
public class XMLParamsFileVariableResolver implements VariableResolver {

	private final String xmlParamsFile;

	/**
	 * @param xmlParamsPath
	 *            Indicates the path of the params file
	 */
	public XMLParamsFileVariableResolver(final String xmlParamsPath) {
		this.xmlParamsFile = xmlParamsPath;
	}

	@Override
	public String resolveVariable(final String variable) throws IllegalArgumentException {
		if (this.xmlParamsFile == null) {
			return null;
		}
		String resolvedString = new String();
		final File xmlFile = new File(this.xmlParamsFile);
		if (!xmlFile.exists()) {
			throw new IllegalArgumentException("Params file " + xmlFile.toString() + " does not exist");
		}
		final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			final NodeList nodeList = doc.getElementsByTagName(variable);
			if (nodeList.getLength() == 0) {
				throw new IllegalArgumentException(
						"The tag: \"" + variable + "\" doesn't exist in the file " + this.xmlParamsFile);
			}

			for (int i = 0; i < nodeList.getLength(); i++) {
				final Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					final Element element = (Element) node;
					resolvedString = element.getTextContent();
				}
			}
			return resolvedString;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}
