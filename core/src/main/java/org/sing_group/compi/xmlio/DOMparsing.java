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
   *          Indicates the path of the XML pipeline file
   * @param xsdFile
   *          Indicates the XSD file
   * @throws SAXException
   *           If there is an error in the XML parsing
   * @throws IOException
   *           If an I/O exception of some sort has occurred
   */
  public static void validateXMLSchema(final String xmlPath, final String xsdFile) throws SAXException, IOException {
    final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema =
      schemaFactory
        .newSchema(DOMparsing.class.getClassLoader().getResource(xsdFile));
    final Validator validator = schema.newValidator();
    validator.setErrorHandler(new SimpleErrorHandler());
    validator.validate(new StreamSource(new File(xmlPath)));
  }

  /**
   * Obtains the schema name of a given XML (xmlns attribute in its root node)
   * 
   * @param xmlPath
   * @return The xmlns attribute of the root node
   * @throws FileNotFoundException
   * @throws SAXException
   * @throws IOException
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

  public static String getSchemaVersion(final String xmlPath) throws FileNotFoundException, SAXException, IOException {
    String schemaName = DOMparsing.getSchemaName(xmlPath.toString());
    if (schemaName == null) {
      throw new IllegalArgumentException("file " + xmlPath + " must declare schema");
    }
    String schemaVersion = schemaName.substring(schemaName.lastIndexOf('-') + 1);

    return schemaVersion;
  }
  

  @SuppressWarnings("serial")
  private static class SAXTermination extends SAXException {}

}
