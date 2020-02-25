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
      XmlSchemaValidation.validateXmlSchema(f.toString(), "xsd/runners-" + schemaVersion + ".xsd");
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
