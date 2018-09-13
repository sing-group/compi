package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.entities.runners.Runners;
import org.sing_group.compi.xmlio.runners.DOMRunnersParser;
import org.xml.sax.SAXException;

public class RunnerTest {

  @Test
  public void testValidRunner() throws SAXException, IOException {
    String runnerFile = ClassLoader.getSystemResource("runners-example.xml").getFile();
    DOMparsing.validateXMLSchema(runnerFile, "xsd/runners.xsd");
  }

  @Test
  public void testJAXB() throws SAXException, IOException, JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Runners.class);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    Runners runners =
      (Runners) jaxbUnmarshaller.unmarshal(new File(ClassLoader.getSystemResource("runners-example.xml").getFile()));

    assertEquals(3, runners.getRunners().size());
    assertEquals("t1", runners.getRunners().get(0).getTasks());
    assertEquals("t2", runners.getRunners().get(1).getTasks());
  }

  @Test
  public void testDOMRunnersParser() throws SAXException, IOException, JAXBException {
    DOMRunnersParser parser = new DOMRunnersParser();
    Runners runners = parser.parseXML(new File(ClassLoader.getSystemResource("runners-example.xml").getFile()));

    assertEquals(3, runners.getRunners().size());
    assertEquals("t1", runners.getRunners().get(0).getTasks());
    assertEquals("t2", runners.getRunners().get(1).getTasks());
  }

  @SuppressWarnings("resource")
  @Test
  public void testSimpleRunnerConfiguration() throws IllegalArgumentException, JAXBException,
    PipelineValidationException, IOException, ParserConfigurationException, SAXException, InterruptedException {
    final File t1ResultFile = new File("/tmp/t1-result");
    final File t2ResultFile = new File("/tmp/t2-result");
    final File t3ResultFile = new File("/tmp/t3-result");
    final File runnerResultFile = new File("/tmp/runner-output");

    t1ResultFile.delete();
    t2ResultFile.delete();
    t3ResultFile.delete();
    runnerResultFile.delete();

    final String pipelineFile = ClassLoader.getSystemResource("runners-pipeline.xml").getFile();
    final String runnersFile = ClassLoader.getSystemResource("runners-example.xml").getFile();

    final CompiApp compi =
      new CompiApp(pipelineFile, 1, TestUtils.resolverFor("my_var", "hello"), null, null, null, null, null);
    compi.setRunnersConfiguration(new File(runnersFile));

    compi.run();

    assertTrue(t1ResultFile.exists());
    assertTrue(t2ResultFile.exists());
    assertTrue(t3ResultFile.exists());
    assertTrue(runnerResultFile.exists());

    String t1_results_contents = new Scanner(t1ResultFile).useDelimiter("\\Z").next();
    String t2_results_contents = new Scanner(t2ResultFile).useDelimiter("\\Z").next();
    String t3_results_contents = new Scanner(t3ResultFile).useDelimiter("\\Z").next();
    String runner_results_contents = new Scanner(runnerResultFile).useDelimiter("\\Z").next();

    assertEquals("hello", t1_results_contents);
    assertEquals("task-2", t2_results_contents);
    assertTrue(t3_results_contents.contains("1"));
    assertTrue(t3_results_contents.contains("2"));
    assertTrue(t3_results_contents.contains("3"));

    assertTrue(runner_results_contents.contains("[t1] my_var: hello code: echo ${my_var} > /tmp/t1-result"));
    assertTrue(runner_results_contents.contains("[t2] code: echo task-2 > /tmp/t2-result"));
    assertTrue(runner_results_contents.contains("[t3] iteration-value: 2 code: echo ${i} ${my_var} >> /tmp/t3-result params: my_var i"));
  }
}
