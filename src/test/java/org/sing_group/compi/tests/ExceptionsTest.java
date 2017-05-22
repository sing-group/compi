package org.sing_group.compi.tests;

import java.io.File;

import org.junit.Test;
import org.sing_group.compi.xmlio.DOMparsing;
import org.xml.sax.SAXException;

import org.sing_group.compi.core.CompiApp;

public class ExceptionsTest {

	@Test(expected = SAXException.class)
	public void testXSDSAXException() throws Exception {
		DOMparsing.validateXMLSchema(ClassLoader.getSystemResource("pipelineParsingException.xml").getFile(),
				new File(ClassLoader.getSystemResource("xsd/pipeline.xsd").getFile()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStringNumberOfThreads() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final String paramsFile = null;
		final String threadNumber = "ass";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeNumberOfThreads() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final String paramsFile = null;
		final String threadNumber = "-2";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeroNumberOfThreads() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final String paramsFile = null;
		final String threadNumber = "0";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParamsException() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineParamsException.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final String threadNumber = "10";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForEachAsNotFoundException() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineForEachNotFoundException.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final String threadNumber = "10";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAdvanceToNonExistantProgram() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipeline.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final String threadNumber = "10";
		final String advanceToProgam = "NonExistantId";
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonExistantDependsOnID() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDependsOnID.xml").getFile();
		final String paramsFile = null;
		final String threadNumber = "10";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonExistantDirectory() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("pipelineNonExistantDirectory.xml").getFile();
		final String paramsFile = ClassLoader.getSystemResource("testParams.xml").getFile();
		final String threadNumber = "10";
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		compi.run(threadNumber, paramsFile, advanceToProgam);
	}
}