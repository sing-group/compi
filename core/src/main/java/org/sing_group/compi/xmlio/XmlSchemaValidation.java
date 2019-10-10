/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
