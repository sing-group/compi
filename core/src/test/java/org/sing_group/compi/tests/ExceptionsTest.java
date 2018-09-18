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
