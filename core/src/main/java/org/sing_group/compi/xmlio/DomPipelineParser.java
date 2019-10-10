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

import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.ParameterDescription;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.pipeline.TaskMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link AbstractPipelineParser} to construtct {@code Pipeline} objects from XML files.
 *
 * @author Jesus Alvarez Casanova
 * @author Hugo López-Fernández
 *
 */
public class DomPipelineParser extends AbstractPipelineParser {

  /**
   * Reads a pipeline XML file and returns it as a {@link Pipeline} object
   *
   * @param f the XML input file
   * @return the parsed Pipeline
   * @throws IllegalArgumentException if a problem in XML parsing and validation occurs
   * @throws IOException if a problem reading the file f occurs
   */
  public Pipeline parseXML(File f) throws IllegalArgumentException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    Pipeline pipeline = new Pipeline();
    try {
      db = dbf.newDocumentBuilder();

      Document doc = db.parse(f);

      NodeList versionNodes = doc.getElementsByTagName("version");
      if (versionNodes.getLength() > 0) {
        pipeline.setVersion(versionNodes.item(0).getTextContent());
      }

      List<ParameterDescription> parameterDescriptions = new LinkedList<>();

      NodeList parameterDescriptionNodes = doc.getElementsByTagName("param");
      for (int i = 0; i < parameterDescriptionNodes.getLength(); i++) {
        Element element = (Element) parameterDescriptionNodes.item(i);
        ParameterDescription description = parseParameter(element);
        if (element.hasAttribute("defaultValue"))
          description.setDefaultValue(element.getAttribute("defaultValue"));
        parameterDescriptions.add(description);
      }

      parameterDescriptionNodes = doc.getElementsByTagName("flag");
      for (int i = 0; i < parameterDescriptionNodes.getLength(); i++) {
        Element element = (Element) parameterDescriptionNodes.item(i);
        ParameterDescription description = parseParameter(element);
        description.setFlag(true);
        parameterDescriptions.add(description);
      }

      pipeline.setParameterDescriptions(parameterDescriptions);

      Map<String, TaskMetadata> taskDescriptions = new HashMap<>();
      if (doc.getElementsByTagName("metadata").getLength() > 0) {
        NodeList metadataNodes = doc.getElementsByTagName("metadata").item(0).getChildNodes();
        for (int i = 0; i < metadataNodes.getLength(); i++) {
          if (metadataNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) metadataNodes.item(i);
            if (element.getTagName().equals("task-description")) {
              String taskDescription = element.getTextContent();
              String taskId = element.getAttribute("id");
              taskDescriptions.put(taskId, new TaskMetadata(taskDescription));
            }
          }
        }
      }

      List<Task> tasks = new LinkedList<Task>();
      if (doc.getElementsByTagName("tasks").getLength() > 0) {
        NodeList tasksChilds = doc.getElementsByTagName("tasks").item(0).getChildNodes();
        for (int i = 0; i < tasksChilds.getLength(); i++) {
          if (tasksChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) tasksChilds.item(i);
            Task task = new Task(pipeline);

            if (element.getNodeName().equals("foreach")) {
              task = new Foreach(pipeline);
              Foreach foreach = (Foreach) task;

              if (element.hasAttribute("of"))
                foreach.setOf(element.getAttribute("of"));
              if (element.hasAttribute("in"))
                foreach.setIn(element.getAttribute("in"));
              if (element.hasAttribute("as"))
                foreach.setAs(element.getAttribute("as"));
            }

            if (element.hasAttribute("id")) {
              task.setMetadata(taskDescriptions.getOrDefault(element.getAttribute("id"), TaskMetadata.EMPTY_METADATA));
              task.setId(element.getAttribute("id"));
            }

            if (element.hasAttribute("after"))
              task.setAfter(element.getAttribute("after"));
            if (element.hasAttribute("interpreter"))
              task.setInterpreter(element.getAttribute("interpreter"));
            if (element.hasAttribute("if"))
              task.setRunIf(element.getAttribute("if"));
            if (element.hasAttribute("src"))
              task.setSrc(element.getAttribute("src"));
            if (element.hasAttribute("params"))
              task.setParametersString(element.getAttribute("params"));

            if (element.getTextContent().trim().length() > 0 && task.getSrc() != null) {
              throw new IllegalArgumentException(
                "Task " + task.getId() + " has both src attribute and body, which is illegal"
              );
            }
            if (task.getSrc() != null) {
              try {
                Path parent = f.toPath().getParent() == null ? new File(".").toPath() : f.toPath().getParent();
                String code = readFileToString(parent.resolve(Paths.get(task.getSrc())).toFile());
                task.setToExecute(code);
              } catch (Exception e) {
                throw new IllegalArgumentException(
                  "Could not read source code from src in task " + task.getId() + " due to: " + e.getMessage()
                );
              }
            } else {
              task.setToExecute(element.getTextContent().trim());
            }

            tasks.add(task);
          }
        }
      }

      pipeline.setTasks(tasks);
      return pipeline;
    } catch (ParserConfigurationException e) {
      throw new IllegalArgumentException(e);
    } catch (SAXException e) {
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      throw e;
    }
  }

  private ParameterDescription parseParameter(Element element) {
    ParameterDescription description = new ParameterDescription();
    if (element.hasAttribute("name"))
      description.setName(element.getAttribute("name"));
    if (element.hasAttribute("shortName"))
      description.setShortName(element.getAttribute("shortName"));
    if (element.hasAttribute("global"))
      description.setGlobal(element.getAttribute("global").equals("true"));
    description.setDescription(element.getTextContent());
    return description;
  }
}
