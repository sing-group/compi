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
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.core.pipeline.Pipeline.fromFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.XmlSchemaValidation;
import org.xml.sax.SAXException;

public class ExceptionsTest {

  private VariableResolver simpleVariableResolver = new VariableResolver() {

    @Override
    public Set<String> getVariableNames() {
      return new HashSet<>();
    }

    @Override
    public String resolveVariable(String variable) throws IllegalArgumentException {
      return "a-simple-value";
    }
  };

  @Test(expected = SAXException.class)
  public void testXSDSAXException() throws Exception {
    XmlSchemaValidation.validateXmlSchema(
      ClassLoader.getSystemResource("pipelineParsingException.xml").getFile(),
      "xsd/pipeline-1.0.xsd"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeNumberOfThreads() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
    final int threadNumber = -2;
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsAMaximumOf(threadNumber)
          .whichResolvesVariablesWith(simpleVariableResolver)
          .build()
      );
    compi.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroNumberOfThreads() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
    final int threadNumber = 0;
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsAMaximumOf(threadNumber)
          .whichResolvesVariablesWith(simpleVariableResolver)
          .build()
      );
    compi.run();
  }

  @Test
  public void testParamsException() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesWith(simpleVariableResolver)
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(1, handler.getAbortedTasks().size());
  }

  @Test
  public void testForEachAsNotFoundException() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineForEachNotFoundException.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesWith(new MapVariableResolver())
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(1, handler.getAbortedTasks().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunFromNonExistantTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipeline.xml").getFile();
    final String fromTask = "NonExistantId";
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .whichResolvesVariablesWith(simpleVariableResolver)
          .build()
      );
    compi.run();
  }

  @Test
  public void testNonExistantDirectory() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDirectory.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesWith(simpleVariableResolver)
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(1, handler.getAbortedTasks().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBothSkipAndRunSingleTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
    final String fromTask = "ID2";
    final String singleTask = "ID2";
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTheSingleTask(singleTask)
          .whichStartsFromTask(fromTask)
          .build()
      );
    compi.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromAndAfterCannotHaveTasksInCommon() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
    final String fromTask = "ID2";
    final String afterTask = "ID2";
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .whichRunsTasksAfterTask(afterTask)
          .build()
      );
    compi.run();
  }
}
