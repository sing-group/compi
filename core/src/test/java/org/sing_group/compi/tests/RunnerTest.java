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
package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.core.pipeline.Pipeline.fromFile;
import static org.sing_group.compi.tests.TestUtils.resolverFor;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.runner.Runners;
import org.sing_group.compi.xmlio.XmlSchemaValidation;
import org.sing_group.compi.xmlio.runners.DOMRunnersParser;
import org.xml.sax.SAXException;

public class RunnerTest {

  @Test
  public void testValidRunner() throws SAXException, IOException {
    String runnerFile = ClassLoader.getSystemResource("runners-example.xml").getFile();
    XmlSchemaValidation.validateXmlSchema(runnerFile, "xsd/runners-1.0.xsd");
  }


  @Test
  public void testDOMRunnersParser() throws SAXException, IOException {
    DOMRunnersParser parser = new DOMRunnersParser();
    Runners runners = parser.parseXML(new File(ClassLoader.getSystemResource("runners-example.xml").getFile()));

    assertEquals(3, runners.getRunners().size());
    assertEquals("t1", runners.getRunners().get(0).getTasks());
    assertEquals("t2", runners.getRunners().get(1).getTasks());
  }

  @SuppressWarnings("resource")
  @Test
  public void testSimpleRunnerConfiguration() throws IllegalArgumentException,
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
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsAMaximumOf(1)
          .whichRunsTasksUsingCustomRunners(new File(runnersFile))
          .whichResolvesVariablesWith(resolverFor("my_var", "hello"))
          .build()
      );
    
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
