package org.sing_group.compi.xmlio.runners;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.xmlio.entities.runners.Runner;
import org.sing_group.compi.xmlio.entities.runners.Runners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DOMRunnersParser implements RunnersParser {

  @Override
  public Runners parseXML(File f) throws IllegalArgumentException, IOException {
    try {

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
