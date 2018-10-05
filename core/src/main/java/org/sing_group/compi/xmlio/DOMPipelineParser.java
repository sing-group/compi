package org.sing_group.compi.xmlio;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.ParameterDescription;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Methods for building {@link Pipeline} objects from files
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class DOMPipelineParser extends AbstractPipelineParser {

  /**
   * Reads a pipeline XML file and returns it as a {@link Pipeline} object
   * 
   * @param f the XML input file
   * @return the parsed Pipeline
   * @throws IllegalArgumentException if a problem in XML parsing and validation
   *           occurs
   * @throws IOException if a problem reading the file f occurs
   */
  public Pipeline parseXML(File f) throws IllegalArgumentException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db;
    Pipeline pipeline = new Pipeline();
    try {
      db = dbf.newDocumentBuilder();

      Document doc = db.parse(f);

      // parameter descriptions

      List<ParameterDescription> parameterDescriptions = new LinkedList<>();
      NodeList parameterDescriptionNodes = doc.getElementsByTagName("param");
      for (int i = 0; i < parameterDescriptionNodes.getLength(); i++) {
        Element element = (Element) parameterDescriptionNodes.item(i);
        ParameterDescription description = new ParameterDescription();
        if (element.hasAttribute("name"))
          description.setName(element.getAttribute("name"));
        if (element.hasAttribute("shortName"))
          description.setShortName(element.getAttribute("shortName"));
        if (element.hasAttribute("defaultValue"))
          description.setDefaultValue(element.getAttribute("defaultValue"));
        if (element.hasAttribute("global"))
          description.setGlobal(element.getAttribute("global").equals("true"));

        description.setDescription(element.getTextContent());

        parameterDescriptions.add(description);
      }
      pipeline.setParameterDescriptions(parameterDescriptions);

      // tasks
      List<Task> tasks = new LinkedList<Task>();
      if (doc.getElementsByTagName("tasks").getLength() > 0) {
        NodeList tasksChilds = doc.getElementsByTagName("tasks").item(0).getChildNodes();
        for (int i = 0; i < tasksChilds.getLength(); i++) {
          if (tasksChilds.item(i).getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) tasksChilds.item(i);
            Task task = new Task();
            if (element.getNodeName().equals("foreach")) {
              task = new Foreach();
              Foreach foreach = (Foreach) task;

              if (element.hasAttribute("of"))
                foreach.setOf(element.getAttribute("of"));
              if (element.hasAttribute("in"))
                foreach.setIn(element.getAttribute("in"));
              if (element.hasAttribute("as"))
                foreach.setAs(element.getAttribute("as"));

            }
            if (element.hasAttribute("id"))
              task.setId(element.getAttribute("id"));
            if (element.hasAttribute("after"))
              task.setAfter(element.getAttribute("after"));
            if (element.hasAttribute("interpreter"))
              task.setInterpreter(element.getAttribute("interpreter"));
            if (element.hasAttribute("fileErrorLog"))
              task.setFileErrorLog(element.getAttribute("fileErrorLog"));
            if (element.hasAttribute("fileLog"))
              task.setFileLog(element.getAttribute("fileLog"));
            if (element.hasAttribute("params"))
              task.setParametersString(element.getAttribute("params"));
            task.setExec(element.getTextContent());

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

}
