package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Validates the XML pipeline file with the XSD file
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class DOMparsing {

	/**
	 * Validates the XML file with the XSD file
	 * 
	 * @param xmlPath
	 *            Indicates the path of the XML pipeline file
	 * @param xsdFile
	 *            Indicates the XSD file
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 */
	public static void validateXMLSchema(final String xmlPath, final String xsdFile) throws SAXException, IOException {
		final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		final Schema schema = schemaFactory
				.newSchema(DOMparsing.class.getClassLoader().getResource(xsdFile));
		final Validator validator = schema.newValidator();
		validator.setErrorHandler(new SimpleErrorHandler());
		validator.validate(new StreamSource(new File(xmlPath)));
	}
}