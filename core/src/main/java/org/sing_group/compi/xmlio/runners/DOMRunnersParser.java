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
package org.sing_group.compi.xmlio.runners;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.runner.Runner;
import org.sing_group.compi.core.runner.Runners;
import org.sing_group.compi.xmlio.XmlSchemaValidation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMRunnersParser implements RunnersParser {

  @Override
  public Runners parseXML(File f) throws IllegalArgumentException, IOException {
    try {
      String schemaVersion = XmlSchemaValidation.getSchemaVersion(f.toString());
      XmlSchemaValidation.validateXmlSchema(f.toString(), "xsd/runners-"+schemaVersion+".xsd");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;
      db = dbf.newDocumentBuilder();
      Document doc = db.parse(f);
      NodeList runnerElementNodes = doc.getElementsByTagName("runner");

      List<Runner> runnersList = new LinkedList<>();
      for (int i = 0; i < runnerElementNodes.getLength(); i++) {
        Element runnerElement = (Element) runnerElementNodes.item(i);
        Runner runner = new Runner();
        if (runnerElement.hasAttribute("tasks")) {
          runner.setTasks(runnerElement.getAttribute("tasks"));
        }
        runner.setRunnerCode(runnerElement.getTextContent());
        
        runnersList.add(runner);
      }

      Runners runners = new Runners();
      runners.setRunners(runnersList);
      return runners;

    } catch (ParserConfigurationException e) {
      throw new IllegalArgumentException(e);
    } catch (SAXException e) {
      throw new IllegalArgumentException(e);
    }
  }

}
