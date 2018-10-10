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
package org.sing_group.compi.tests;

import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.xmlio.entities.Pipeline.fromFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.DOMparsing;
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
    DOMparsing.validateXMLSchema(
      ClassLoader.getSystemResource("pipelineParsingException.xml").getFile(),
      "xsd/pipeline.xsd"
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

  @Test(expected = IllegalArgumentException.class)
  public void testParamsException() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
    final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesFromFile(new File(paramsFile))
          .build()
      );
    compi.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForEachAsNotFoundException() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineForEachNotFoundException.xml").getFile();
    final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesFromFile(new File(paramsFile))
          .build()
      );
    compi.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunFromNonExistantTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipeline.xml").getFile();
    final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
    final String fromTask = "NonExistantId";
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesFromFile(new File(paramsFile))
          .whichStartsFromTask(fromTask)
          .build()
      );
    compi.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonExistantDirectory() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDirectory.xml").getFile();
    final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesFromFile(new File(paramsFile))
          .build()
      );
    
    compi.run();
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
