package org.sing_group.compi.tests;

import static java.util.Arrays.asList;

import java.util.Arrays;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.sing_group.compi.core.VariableResolver;
import org.sing_group.compi.xmlio.DOMparsing;
import org.xml.sax.SAXException;

public class ExceptionsTest {

	private VariableResolver simpleVariableResolver = new VariableResolver() {

		@Override
		public String resolveVariable(String variable) throws IllegalArgumentException {
			return "a-simple-value";
		}

	};

	@Test(expected = SAXException.class)
	public void testXSDSAXException() throws Exception {
		DOMparsing.validateXMLSchema(ClassLoader.getSystemResource("pipelineParsingException.xml").getFile(),
				"xsd/pipeline.xsd");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeNumberOfThreads() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final int threadNumber = -2;
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, simpleVariableResolver, null, null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeroNumberOfThreads() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final int threadNumber = 0;
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, simpleVariableResolver, null, null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParamsException() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final int threadNumber = 10;
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, paramsFile, null, null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForEachAsNotFoundException() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineForEachNotFoundException.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final int threadNumber = 10;
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, paramsFile, null, null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdvanceToNonExistantTask() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipeline.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final int threadNumber = 10;
		final String fromTasks = "NonExistantId";
		final CompiApp compi = new CompiApp(pipelineFile,threadNumber, paramsFile, Arrays.asList(fromTasks), null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonExistantDependsOnID() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDependsOnID.xml").getFile();
		final int threadNumber = 10;
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, simpleVariableResolver, null, null, null, null);
		compi.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonExistantDirectory() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDirectory.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final int threadNumber = 10;
		try {
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, paramsFile, null, null, null, null);
		compi.run();
		} catch(PipelineValidationException e) {
		  System.err.println(e.getErrors());
		  e.printStackTrace();
		  throw e;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBothSkipAndRunSingleTask() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
		final String paramsFile = null;
		final int threadNumber = -2;
		final String advanceToProgam = "ID2";
		final String singleTask = "ID2";
		final CompiApp compi = new CompiApp(pipelineFile, threadNumber, paramsFile, asList(advanceToProgam), singleTask, null, null);
		compi.run();
	}
}
