package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.sing_group.compi.xmlio.entities.Program;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.ProgramExecutionHandler;

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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();

			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 1);
		assertEquals(finishedPrograms.get(), 1);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();

			}
			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				e.printStackTrace();
			}

		});

		File outFile = File.createTempFile("compi-test", ".txt");
		outFile.deleteOnExit();
		compi.run(THREAD_NUMBER, (var) -> {
			switch(var) {
				case "text": return "hello";
				case "destination": return outFile.toString();
			}
			return null;
		}, advanceToProgam);

		assertEquals(1, startedPrograms.get());
		assertEquals(1, finishedPrograms.get());


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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final Map<String, Long> times = new HashMap<>();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
				times.put(program.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			}

			@Override
			public void programAborted(Program program, Exception e) {
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID2"));
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID3"));
		assertTrue("program does not wait for its dependency", times.get("ID2") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID3") < times.get("ID4"));
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 4);
	}

	@Test
	public void testPipelineLoop() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testPipelineLoopWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testPipelineLoop.xml").getFile();
		}
		final String advanceToProgam = null;
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final Map<String, Long> times = new HashMap<>();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				if (program.getForeach() != null) {
					final Program parent = compi.getParentProgram().get(program);
					if (parent.isFinished()) {
						finishedPrograms.incrementAndGet();
						times.put(parent.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					}
				} else {
					finishedPrograms.incrementAndGet();
					times.put(program.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
				}
			}

			@Override
			public void programAborted(Program program, Exception e) {
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID2"));
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID3"));
		assertTrue("program does not wait for its dependency", times.get("ID2") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID3") < times.get("ID4"));
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 4);
	}

	@Test
	public void testSkipPrograms() throws Exception {
		final String pipelineFile;
		if (isWindows()) {
			pipelineFile = ClassLoader.getSystemResource("testSkipProgramsWindows.xml").getFile();
		} else {
			pipelineFile = ClassLoader.getSystemResource("testSkipPrograms.xml").getFile();
		}
		final String advanceToProgam = "ID4";
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger skippedPrograms = new AtomicInteger();
		final Map<String, Long> times = new HashMap<>();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				if (program.isSkipped()) {
					skippedPrograms.incrementAndGet();
				}
				finishedPrograms.incrementAndGet();
				times.put(program.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			}

			@Override
			public void programAborted(Program program, Exception e) {
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID2") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID3") < times.get("ID4"));
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 4);
		assertEquals(skippedPrograms.get(), 3);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger skippedPrograms = new AtomicInteger();
		final Map<String, Long> times = new HashMap<>();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				if (program.getForeach() != null) {
					final Program parent = compi.getParentProgram().get(program);
					if (parent.isFinished()) {
						finishedPrograms.incrementAndGet();
						times.put(parent.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
						if (parent.isSkipped()) {
							skippedPrograms.incrementAndGet();
						}
					}
				} else {
					finishedPrograms.incrementAndGet();
					times.put(program.getId(), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
					if (program.isSkipped()) {
						skippedPrograms.incrementAndGet();
					}
				}
			}

			@Override
			public void programAborted(Program program, Exception e) {
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertTrue("program does not wait for its dependency", times.get("ID1") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID2") < times.get("ID4"));
		assertTrue("program does not wait for its dependency", times.get("ID3") < times.get("ID4"));
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 4);
		assertEquals(skippedPrograms.get(), 3);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger abortedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				abortedPrograms.incrementAndGet();
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 1);
		assertEquals(finishedPrograms.get(), 0);
		assertEquals(abortedPrograms.get(), 4);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger abortedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				abortedPrograms.incrementAndGet();
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 3);
		assertEquals(finishedPrograms.get(), 1);
		assertEquals(abortedPrograms.get(), 2);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger abortedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				abortedPrograms.incrementAndGet();
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 3);
		assertEquals(finishedPrograms.get(), 3);
		assertEquals(abortedPrograms.get(), 2);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger abortedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				abortedPrograms.incrementAndGet();
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 3);
		assertEquals(abortedPrograms.get(), 2);
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
		final CompiApp compi = new CompiApp(pipelineFile);
		final AtomicInteger startedPrograms = new AtomicInteger();
		final AtomicInteger finishedPrograms = new AtomicInteger();
		final AtomicInteger abortedPrograms = new AtomicInteger();
		compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

			@Override
			public void programStarted(Program program) {
				startedPrograms.incrementAndGet();
			}

			@Override
			public void programFinished(Program program) {
				finishedPrograms.incrementAndGet();
			}

			@Override
			public void programAborted(Program program, Exception e) {
				abortedPrograms.incrementAndGet();
			}

		});
		compi.run(THREAD_NUMBER, (String) null, advanceToProgam);
		assertEquals(startedPrograms.get(), 4);
		assertEquals(finishedPrograms.get(), 3);
		assertEquals(abortedPrograms.get(), 4);
	}

}
