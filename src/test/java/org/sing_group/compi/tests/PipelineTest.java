package org.sing_group.compi.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;

public class PipelineTest {
	private final static int THREAD_NUMBER = 6;

	@Test
	public void testOneTask() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testOneTask.xml").getFile();
		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(1, handler.getFinishedTasks().size());
	}

	@Test
	public void testParameters() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("echoPipeline.xml").getFile();
		final String advanceToTask = null;
		File outFile = File.createTempFile("compi-test", ".txt");

		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (var) -> {
			switch (var) {
			case "text":
				return "hello";
			case "destination":
				return outFile.toString();
			}
			return null;
		}, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);
		outFile.deleteOnExit();

		compi.run();

		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(1, handler.getFinishedTasks().size());

	}

	@Test
	public void testSimplePipeline() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSimplePipeline.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID2"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID3"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID2") < handler.getFinishedTasks().indexOf("ID4"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID3") < handler.getFinishedTasks().indexOf("ID4"));
		assertEquals(4, handler.getStartedTasks().size());
		assertEquals(4, handler.getFinishedTasks().size());
	}

	@Test
	public void testPipelineLoop() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testPipelineLoop.xml").getFile();

		File[] filesToTouch = new File[] { File.createTempFile("compi-test", ".txt"),
				File.createTempFile("compi-test", ".txt"), File.createTempFile("compi-test", ".txt") };
		String elementsValue = filesToTouch[0].toString()+","+filesToTouch[1].toString()+","+filesToTouch[2].toString();
		
		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (var) -> (var.equals("elements"))? elementsValue: (var.equals("dirparam")? filesToTouch[0].getParent().toString():null), advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID2"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID3"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID2") < handler.getFinishedTasks().indexOf("ID4"));
		assertTrue("task does not wait for its dependency",
				handler.getFinishedTasks().indexOf("ID3") < handler.getFinishedTasks().indexOf("ID4"));
		assertEquals(handler.getStartedTasks().size(), 6);
		assertEquals(handler.getFinishedTasks().size(), 6);
		
		for (File f: filesToTouch) {
			assertTrue(f.exists());
		}
		
		for (File f: filesToTouch) {
			assertTrue(new File(f.toString()+".2.txt").exists());
		}
	}

	@Test
	public void testSkipTasks() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
		final String advanceToTask = "ID3";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(3, handler.getStartedTasks().size());
		assertEquals(3, handler.getFinishedTasks().size());
		assertTrue(handler.getFinishedTasks().containsAll(Arrays.asList("ID3", "ID4", "ID2")));
	}

	@Test
	public void testSkipTaskWithLoops() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSkipTasksWithLoops.xml").getFile();

		final String advanceToTask = "ID4";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(1, handler.getFinishedTasks().size());
		assertEquals("ID4", handler.getFinishedTasks().get(0));
	}

	@Test
	public void testRunSingleTask() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();

		final String singleTask = "ID3";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, null, singleTask);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();
		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(1, handler.getFinishedTasks().size());
	}

	@Test
	public void testStartingTaskAborted() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testStartingTaskAborted.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(0, handler.getFinishedTasks().size());
		assertEquals(4, handler.getAbortedTasks().size());
	}

	@Test
	public void testTasksAborted() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testTasksAborted.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();
		assertEquals(3, handler.getStartedTasks().size());
		assertEquals(2, handler.getFinishedTasks().size());
		assertEquals(2, handler.getAbortedTasks().size());
	}

	@Test
	public void testTasksAbortedWithLoops() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testTasksAbortedWithLoops.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();
		assertEquals(3, handler.getStartedTasks().size());

		assertEquals(5, handler.getFinishedTasksExcludingLoopChildren().size());
		assertEquals(2, handler.getAbortedTasks().size());
	}

	@Test
	public void testSomeTasksAbortedAndContinue() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSomeTasksAbortedAndContinue.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedTasks().size());
		assertEquals(3, handler.getFinishedTasks().size());
		assertEquals(2, handler.getAbortedTasks().size());
	}

	@Test
	public void testSomeTasksAbortedAndContinueWithLoops() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSomeTasksAbortedAndContinueWithLoops.xml").getFile();
		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedTasks().size());
		assertEquals(3, handler.getFinishedTasksExcludingLoopChildren().size());
		assertEquals(4, handler.getAbortedTasks().size());
	}

	private static class TestExecutionHandler implements TaskExecutionHandler {
		final List<String> startedTasks = new ArrayList<>();
		final List<String> finishedTasks = new ArrayList<>();
		final List<String> finishedTasksExcludingLoopChildren = new ArrayList<>();
		final List<String> abortedTasks = new ArrayList<>();
		private CompiApp compi;

		public TestExecutionHandler(CompiApp compi) {
			this.compi = compi;
		}

		@Override
		public void taskStarted(Task task) {
			startedTasks.add(task.getId());
		}

		@Override
		public void taskFinished(Task task) {
			if (task instanceof Foreach) {
				final Task parent = compi.getParentTask().get(task);
				if (parent.isFinished()) {
					finishedTasks.add(parent.getId());
				}
			} else {
				finishedTasks.add(task.getId());
			}

			finishedTasksExcludingLoopChildren.add(task.getId());
		}

		@Override
		public void taskAborted(Task task, Exception e) {
			abortedTasks.add(task.getId());
		}

		public List<String> getStartedTasks() {
			return startedTasks;
		}

		public List<String> getFinishedTasks() {
			return finishedTasks;
		}

		public List<String> getAbortedTasks() {
			return abortedTasks;
		}

		public List<String> getFinishedTasksExcludingLoopChildren() {
			return finishedTasksExcludingLoopChildren;
		}

	}

}
