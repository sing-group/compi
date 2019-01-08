/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class offers methods related with XSD file validation.
 * 
 * @author Jesus Alvarez Casanova
 * @author Hugo López-Fernández
 *
 */
public class XmlSchemaValidation {

  /**
   * Validates the XML file with the XSD file.
   * 
   * @param xmlPath the path of the XML pipeline file
   * @param xsdFile the XSD file
   * @throws SAXException if there is an error in the XML parsing
   * @throws IOException if an I/O exception of some sort has occurred
   */
  public static void validateXmlSchema(final String xmlPath, final String xsdFile) throws SAXException, IOException {
    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema =
      schemaFactory
        .newSchema(XmlSchemaValidation.class.getClassLoader().getResource(xsdFile));
    final Validator validator = schema.newValidator();
    validator.setErrorHandler(new SimpleErrorHandler());
    validator.validate(new StreamSource(new File(xmlPath)));
  }

  /**
   * Returns {@code true} if the specified XSD file exists and {@code false} otherwise.
   * 
   * @param xsdFile the XSD file
   * @return {@code true} if the specified XSD file exists and {@code false} otherwise
   */
  public static boolean existsSchema(final String xsdFile) {
    return XmlSchemaValidation.class.getClassLoader().getResource(xsdFile) != null;
  }

  /**
   * Returns the schema name of a given XML (xmlns attribute in its root node).
   * 
   * @param xmlPath the path of the XML pipeline file
   * @return the xmlns attribute of the root node
   * @throws FileNotFoundException if the pipeline file does not exist
   * @throws SAXException if there is an error in the XML parsing
   * @throws IOException if an I/O exception of some sort has occurred
   */
  public static String getSchemaName(final String xmlPath) throws FileNotFoundException, SAXException, IOException {
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    SAXParser saxParser;
    final StringBuilder xmlns = new StringBuilder();
    try {
      saxParser = saxParserFactory.newSAXParser();
      saxParser.parse(new FileInputStream(new File(xmlPath)), new DefaultHandler() {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException {
          // the first startElementn event corresponds to the root element...
          String xmlnsAtt = attributes.getValue("xmlns");
          if (xmlnsAtt != null) {
            xmlns.append(xmlnsAtt);
          }
          throw new SAXTermination(); // force parsing termination
        }
      });
      return null;
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXTermination e) {
      String xmlnsValue = xmlns.toString();
      return xmlnsValue.length() > 0 ? xmlnsValue : null;
    }
  }

  /**
   * Returns the schema version of a given XML.
   * 
   * @param xmlPath the path of the XML pipeline file
   * @return the schema version
   * @throws FileNotFoundException if the pipeline file does not exist
   * @throws SAXException if there is an error in the XML parsing
   * @throws IOException if an I/O exception of some sort has occurred
   */
  public static String getSchemaVersion(final String xmlPath) throws FileNotFoundException, SAXException, IOException {
    String schemaName = XmlSchemaValidation.getSchemaName(xmlPath.toString());
    if (schemaName == null) {
      throw new IllegalArgumentException("file " + xmlPath + " must declare schema");
    }
    String schemaVersion = schemaName.substring(schemaName.lastIndexOf('-') + 1);

    return schemaVersion;
  }

  @SuppressWarnings("serial")
  private static class SAXTermination extends SAXException {}

}
