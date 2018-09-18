package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Obtains the command to execute
 *
 * @author Jesus Alvarez Casanova
 * @author Daniel Glez-Peña
 */
public class XMLParamsFileVariableResolver implements VariableResolver {

	private File xmlParamsFile;

	private MapVariableResolver mapResolver;
	/**
	 * @param xmlParamsFile
	 *            Indicates the path of the params file
	 */
	public XMLParamsFileVariableResolver(final File xmlParamsFile) {
		if (xmlParamsFile != null) {
		  this.xmlParamsFile = xmlParamsFile;
		  if (!this.xmlParamsFile.exists()) {
	      throw new IllegalArgumentException("Params file " + this.xmlParamsFile.toString() + " does not exist");
	    }
		  this.mapResolver = new MapVariableResolver(parseXML());
		} else {
		  this.mapResolver = new MapVariableResolver();
		}
	}

	private Map<String, String> parseXML() {
    try {
      final Map<String, String> variables = new HashMap<>();
      final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder dBuilder;
      dBuilder = dbFactory.newDocumentBuilder();
      final Document doc = dBuilder.parse(this.xmlParamsFile);
      doc.getDocumentElement().normalize();
      Element rootElem = (Element) doc.getElementsByTagName("pipeline_params").item(0);
      NodeList children = rootElem.getChildNodes();
      for (int i = 0; i< children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          String varName = node.getNodeName();
          String varValue = node.getTextContent().trim();
          variables.put(varName, varValue);
        }
      }
      return variables;
      
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }

	@Override
	public String resolveVariable(String variable) throws IllegalArgumentException {
	  return this.mapResolver.resolveVariable(variable);
	}
	
	@Override
	public Set<String> getVariableNames() {
	  return this.mapResolver.getVariableNames();
	}
	
  /*@Override
	public String resolveVariable(final String variable) throws IllegalArgumentException {
		if (this.xmlParamsFile == null) {
			return null;
		}
		String resolvedString = new String();
		final File xmlFile = this.xmlParamsFile;
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
				//throw new IllegalArgumentException(
				//		"The tag: \"" + variable + "\" doesn't exist in the file " + this.xmlParamsFile);
			  return null;
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
	}*/

}
