package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.ProgramExecutionHandler;
import org.sing_group.compi.xmlio.entities.Program;

public class PipelineTest {
	private final static int THREAD_NUMBER = 6;
	private final static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	@Test
	public void testOneProgram() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testOneProgramWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testOneProgram.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedPrograms().size());
		assertEquals(1, handler.getFinishedPrograms().size());
	}

	@Test
	public void testParameters() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("echoPipelineWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("echoPipeline.xml").getFile();
		}

		final String advanceToProgam = null;
		File outFile = File.createTempFile("compi-test", ".txt");

		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (var) -> {
			switch (var) {
			case "text":
				return "hello";
			case "destination":
				return outFile.toString();
			}
			return null;
		}, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);
		outFile.deleteOnExit();

		compi.run();

		assertEquals(1, handler.getStartedPrograms().size());
		assertEquals(1, handler.getFinishedPrograms().size());

	}

	@Test
	public void testSimplePipeline() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSimplePipelineWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSimplePipeline.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID1") < handler.getFinishedPrograms().indexOf("ID2"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID1") < handler.getFinishedPrograms().indexOf("ID3"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID2") < handler.getFinishedPrograms().indexOf("ID4"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID3") < handler.getFinishedPrograms().indexOf("ID4"));
		assertEquals(4, handler.getStartedPrograms().size());
		assertEquals(4, handler.getFinishedPrograms().size());
	}

	@Test
	public void testPipelineLoop() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testPipelineLoopWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testPipelineLoop.xml").getFile();
		}

		File[] filesToTouch = new File[] { File.createTempFile("compi-test", ".txt"),
				File.createTempFile("compi-test", ".txt"), File.createTempFile("compi-test", ".txt") };
		String elementsValue = filesToTouch[0].toString()+","+filesToTouch[1].toString()+","+filesToTouch[2].toString();
		
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (var) -> (var.equals("elements"))? elementsValue: (var.equals("dirparam")? filesToTouch[0].getParent().toString():null), advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID1") < handler.getFinishedPrograms().indexOf("ID2"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID1") < handler.getFinishedPrograms().indexOf("ID3"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID2") < handler.getFinishedPrograms().indexOf("ID4"));
		assertTrue("program does not wait for its dependency",
				handler.getFinishedPrograms().indexOf("ID3") < handler.getFinishedPrograms().indexOf("ID4"));
		assertEquals(handler.getStartedPrograms().size(), 6);
		assertEquals(handler.getFinishedPrograms().size(), 6);
		
		for (File f: filesToTouch) {
			assertTrue(f.exists());
		}
		
		for (File f: filesToTouch) {
			assertTrue(new File(f.toString()+".2.txt").exists());
		}
	}

	@Test
	public void testSkipPrograms() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSkipProgramsWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSkipPrograms.xml").getFile();
		}
		final String advanceToProgam = "ID3";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(3, handler.getStartedPrograms().size());
		assertEquals(3, handler.getFinishedPrograms().size());
		assertTrue(handler.getFinishedPrograms().containsAll(Arrays.asList("ID3", "ID4", "ID2")));
	}

	@Test
	public void testSkipProgramWithLoops() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSkipProgramsWithLoopsWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSkipProgramsWithLoops.xml").getFile();
		}

		final String advanceToProgam = "ID4";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedPrograms().size());
		assertEquals(1, handler.getFinishedPrograms().size());
		assertEquals("ID4", handler.getFinishedPrograms().get(0));
	}

	@Test
	public void testRunSingleProgram() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSkipProgramsWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSkipPrograms.xml").getFile();
		}

		final String singleProgram = "ID3";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, null, singleProgram);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();
		assertEquals(1, handler.getStartedPrograms().size());
		assertEquals(1, handler.getFinishedPrograms().size());
	}

	@Test
	public void testStartingProgramAborted() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testStartingProgramAbortedWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testStartingProgramAborted.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedPrograms().size());
		assertEquals(0, handler.getFinishedPrograms().size());
		assertEquals(4, handler.getAbortedPrograms().size());
	}

	@Test
	public void testProgramsAborted() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testProgramsAbortedWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testProgramsAborted.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();
		assertEquals(3, handler.getStartedPrograms().size());
		assertEquals(2, handler.getFinishedPrograms().size());
		assertEquals(2, handler.getAbortedPrograms().size());
	}

	@Test
	public void testProgramsAbortedWithLoops() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testProgramsAbortedWithLoopsWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testProgramsAbortedWithLoops.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();
		assertEquals(3, handler.getStartedPrograms().size());

		assertEquals(5, handler.getFinishedProgramsExcludingLoopChildren().size());
		assertEquals(2, handler.getAbortedPrograms().size());
	}

	@Test
	public void testSomeProgramsAbortedAndContinue() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSomeProgramsAbortedAndContinueWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSomeProgramsAbortedAndContinue.xml").getFile();
		}

		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedPrograms().size());
		assertEquals(3, handler.getFinishedPrograms().size());
		assertEquals(2, handler.getAbortedPrograms().size());
	}

	@Test
	public void testSomeProgramsAbortedAndContinueWithLoops() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSomeProgramsAbortedAndContinueWithLoopsWindows.xml")
					.getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSomeProgramsAbortedAndContinueWithLoops.xml").getFile();
		}
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToProgam, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addProgramExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedPrograms().size());
		assertEquals(3, handler.getFinishedProgramsExcludingLoopChildren().size());
		assertEquals(4, handler.getAbortedPrograms().size());
	}

	private static class TestExecutionHandler implements ProgramExecutionHandler {
		final List<String> startedPrograms = new ArrayList<>();
		final List<String> finishedPrograms = new ArrayList<>();
		final List<String> finishedProgramsExcludingLoopChildren = new ArrayList<>();
		final List<String> abortedPrograms = new ArrayList<>();
		private CompiApp compi;

		public TestExecutionHandler(CompiApp compi) {
			this.compi = compi;
		}

		@Override
		public void programStarted(Program program) {
			startedPrograms.add(program.getId());
		}

		@Override
		public void programFinished(Program program) {
			if (program.getForeach() != null) {
				final Program parent = compi.getParentProgram().get(program);
				if (parent.isFinished()) {
					finishedPrograms.add(parent.getId());
				}
			} else {
				finishedPrograms.add(program.getId());
			}

			finishedProgramsExcludingLoopChildren.add(program.getId());
		}

		@Override
		public void programAborted(Program program, Exception e) {
			abortedPrograms.add(program.getId());
		}

		public List<String> getStartedPrograms() {
			return startedPrograms;
		}

		public List<String> getFinishedPrograms() {
			return finishedPrograms;
		}

		public List<String> getAbortedPrograms() {
			return abortedPrograms;
		}

		public List<String> getFinishedProgramsExcludingLoopChildren() {
			return finishedProgramsExcludingLoopChildren;
		}

	}

}
