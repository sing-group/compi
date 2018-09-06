package org.sing_group.compi.tests;

import static java.io.File.createTempFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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
		}, advanceToTask, null, null, null);
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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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

		File[] filesToTouch = new File[] { createTempFile("compi-test", ".txt"), createTempFile("compi-test", ".txt"),
				createTempFile("compi-test", ".txt") };
		for (File f : filesToTouch) {
			f.deleteOnExit();
		}

		String elementsValue = filesToTouch[0].toString() + "," + filesToTouch[1].toString() + ","
				+ filesToTouch[2].toString();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER,
				(var) -> (var.equals("elements")) ? elementsValue
						: (var.equals("dirparam") ? filesToTouch[0].getParent().toString() : null),
				advanceToTask, null, null, null);
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

		for (File f : filesToTouch) {
			assertTrue(f.exists());
		}

		for (File f : filesToTouch) {
			assertTrue(new File(f.toString() + ".2").exists());
		}
	}

	@Test
	public void testSkipTasks() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
		final String advanceToTask = "ID3";
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, null, singleTask, null, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();
		assertEquals(1, handler.getStartedTasks().size());
		assertEquals(1, handler.getFinishedTasks().size());
	}
	
  @Test
  public void testRunUntilTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testRunUntilTask.xml").getFile();

    final String untilTask = "ID3";
    final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, null, null, untilTask, null);
    TestExecutionHandler handler = new TestExecutionHandler(compi);
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(2, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("ID1"));
    assertTrue(handler.getStartedTasks().contains("ID3"));
    assertEquals(2, handler.getFinishedTasks().size());
  }
  
  @Test
  public void testUntilStartingFrom() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String fromTask = "task-3";
    final String untilTask = "task-6";
    final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, fromTask, null, untilTask, null);
    TestExecutionHandler handler = new TestExecutionHandler(compi);
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(2, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-3"));
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertEquals(2, handler.getFinishedTasks().size());
  }
  
  @Test
  public void testBeforeStartingFrom() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String fromTask = "task-3";
    final String beforeTask = "task-8";
    final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, fromTask, null, null, beforeTask);
    TestExecutionHandler handler = new TestExecutionHandler(compi);
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(6, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-2"));
    assertTrue(handler.getStartedTasks().contains("task-3"));
    assertTrue(handler.getStartedTasks().contains("task-4"));
    assertTrue(handler.getStartedTasks().contains("task-5"));
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertTrue(handler.getStartedTasks().contains("task-7"));
    assertEquals(6, handler.getFinishedTasks().size());
  }
  
  @Test
  public void testStartingFromAndUntilSameTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String fromTask = "task-8";
    final String untilTask = "task-8";
    final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, fromTask, null, untilTask, null);
    TestExecutionHandler handler = new TestExecutionHandler(compi);
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(1, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-8"));
    assertEquals(1, handler.getFinishedTasks().size());
  }
  
  @Test
  public void testRunBeforeTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testRunUntilTask.xml").getFile();

    final String beforeTask = "ID4";
    final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, null, null, null, beforeTask);
    TestExecutionHandler handler = new TestExecutionHandler(compi);
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(3, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("ID1"));
    assertTrue(handler.getStartedTasks().contains("ID2"));
    assertTrue(handler.getStartedTasks().contains("ID3"));
    assertEquals(3, handler.getFinishedTasks().size());
  }
  
	@Test
	public void testStartingTaskAborted() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testStartingTaskAborted.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
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
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();
		assertEquals(3, handler.getStartedTasks().size());

		assertEquals(5, handler.getFinishedTasksIncludingLoopChildren().size());
		assertEquals(2, handler.getAbortedTasks().size());
	}

	@Test
	public void testSomeTasksAbortedAndContinue() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSomeTasksAbortedAndContinue.xml").getFile();

		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedTasks().size());
		assertEquals(3, handler.getFinishedTasks().size());
		assertEquals(2, handler.getAbortedTasks().size());
	}

	@Test
	public void testSomeTasksAbortedAndContinueWithLoops() throws Exception {
		final String pipelineFile = ClassLoader.getSystemResource("testSomeTasksAbortedAndContinueWithLoops.xml")
				.getFile();
		final String advanceToTask = null;
		final CompiApp compi = new CompiApp(pipelineFile, THREAD_NUMBER, (String) null, advanceToTask, null, null, null);
		TestExecutionHandler handler = new TestExecutionHandler(compi);
		compi.addTaskExecutionHandler(handler);

		compi.run();

		assertEquals(4, handler.getStartedTasks().size());
		assertEquals(3, handler.getFinishedTasksIncludingLoopChildren().size());
		assertEquals(4, handler.getAbortedTasks().size());
	}

	private static class TestExecutionHandler implements TaskExecutionHandler {
		final List<String> startedTasks = new ArrayList<>();
		final List<String> finishedTasks = new ArrayList<>();
		final List<String> finishedTasksIncludingLoopChildren = new ArrayList<>();
		final List<String> abortedTasks = new ArrayList<>();
		final Set<String> startedForeachs = new HashSet<>();
		private CompiApp compi;

		public TestExecutionHandler(CompiApp compi) {
			this.compi = compi;
		}

		@Override
		synchronized public void taskStarted(Task task) {
			if (task instanceof Foreach && !startedForeachs.contains(task.getId())) {
				startedTasks.add(task.getId());
				startedForeachs.add(task.getId());
			} else if (!(task instanceof Foreach)) {
				startedTasks.add(task.getId());
			}
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

			finishedTasksIncludingLoopChildren.add(task.getId());
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

		public List<String> getFinishedTasksIncludingLoopChildren() {
			return finishedTasksIncludingLoopChildren;
		}

	}

}
