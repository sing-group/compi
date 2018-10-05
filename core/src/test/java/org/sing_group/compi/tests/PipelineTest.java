package org.sing_group.compi.tests;

import static java.io.File.createTempFile;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.tests.TestUtils.resolverFor;
import static org.sing_group.compi.xmlio.entities.Pipeline.fromFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Task;

public class PipelineTest {

  @Test
  public void testOneTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testOneTask.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(1, handler.getStartedTasks().size());
    assertEquals(1, handler.getFinishedTasks().size());
  }

  @Test
  public void testParameters() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("echoPipeline.xml").getFile();
    File outFile = File.createTempFile("compi-test", ".txt");

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesWith(
            resolverFor(
              "text", "hello",
              "destination", outFile.toString()
            )
          )
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);
    outFile.deleteOnExit();

    compi.run();

    assertTrue(outFile.exists());
    assertTrue(outFile.length() > 0);
    try (Scanner outFileScanner = new Scanner(outFile)) {
      assertEquals("hello", outFileScanner.nextLine());
    }
    assertEquals(1, handler.getStartedTasks().size());
    assertEquals(1, handler.getFinishedTasks().size());

  }

  @Test
  public void testSimplePipeline() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSimplePipeline.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID2")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID3")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID2") < handler.getFinishedTasks().indexOf("ID4")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID3") < handler.getFinishedTasks().indexOf("ID4")
    );
    assertEquals(4, handler.getStartedTasks().size());
    assertEquals(4, handler.getFinishedTasks().size());

  }

  @Test
  public void testPipelineLoop() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testPipelineLoop.xml").getFile();

    File[] filesToTouch = new File[] {
      createTempFile("compi-test", ".txt"), createTempFile("compi-test", ".txt"),
      createTempFile("compi-test", ".txt")
    };
    for (File f : filesToTouch) {
      f.deleteOnExit();
    }

    String elementsValue =
      filesToTouch[0].toString() + "," + filesToTouch[1].toString() + ","
        + filesToTouch[2].toString();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichResolvesVariablesWith(
            resolverFor(
              "elements", elementsValue,
              "dirparam", filesToTouch[0].getParent().toString()
            )
          )
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID2")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID1") < handler.getFinishedTasks().indexOf("ID3")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID2") < handler.getFinishedTasks().indexOf("ID4")
    );
    assertTrue(
      "task does not wait for its dependency",
      handler.getFinishedTasks().indexOf("ID3") < handler.getFinishedTasks().indexOf("ID4")
    );
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
  public void testTaskExecutionHandler() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testExecutionHandler.xml").getFile();
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
        .whichRunsUntilTask("ID4") //so we check that do not receive any event from ID5
        .whichRunsAMaximumOf(1)
          .build()
      );

    Map<String, Task> tasksById = compi.getPipeline().getTasks().stream().collect(Collectors.toMap(Task::getId, Function.identity()));
  
//    TestExecutionHandler handler = new TestExecutionHandler();
    
    @SuppressWarnings("unchecked")
    final Capture<ForeachIteration>[] capturesForTaskID1 = new Capture[6];
    for (int i = 0; i < capturesForTaskID1.length; i++) {
      capturesForTaskID1[i] = newCapture();
    }
    @SuppressWarnings("unchecked")
    final Capture<ForeachIteration>[] capturesForTaskID3 = new Capture[4];
    for (int i = 0; i < capturesForTaskID3.length; i++) {
      capturesForTaskID3[i] = newCapture();
    }
    @SuppressWarnings("unchecked")
    final Capture<ForeachIteration>[] capturesForTaskID4 = new Capture[6];
    for (int i = 0; i < capturesForTaskID4.length; i++) {
      capturesForTaskID4[i] = newCapture();
    }
    
    TaskExecutionHandler handler = EasyMock.mock(TaskExecutionHandler.class);
    
    // ID1
    
    // the whole loop task ID1 starts...
    handler.taskStarted(tasksById.get("ID1"));
    expectLastCall();
    
    // ID1: iteration 1, which ends normally
    handler.taskIterationStarted(capture(capturesForTaskID1[0]));
    expectLastCall();
    handler.taskIterationFinished(capture(capturesForTaskID1[1]));
    expectLastCall();
    
    // ID1: iteration 2, which ends normally
    handler.taskIterationStarted(capture(capturesForTaskID1[2]));
    expectLastCall();
    handler.taskIterationFinished(capture(capturesForTaskID1[3]));
    expectLastCall();
    
    // ID1: iteration 3, which ends normally
    handler.taskIterationStarted(capture(capturesForTaskID1[4]));
    expectLastCall();
    handler.taskIterationFinished(capture(capturesForTaskID1[5]));
    expectLastCall();
    
    // The whole task ID1 ends normally
    handler.taskFinished(tasksById.get("ID1"));
    expectLastCall();
    
    // ID2, a simple task which starts and ends normally
    handler.taskStarted(tasksById.get("ID2"));
    expectLastCall();
    handler.taskFinished(tasksById.get("ID2"));
    expectLastCall();
    
    // ID3
    handler.taskStarted(tasksById.get("ID3"));
    expectLastCall();
    handler.taskIterationStarted(capture(capturesForTaskID3[0]));
    expectLastCall();
    
    // ID3: iteration 1, which aborts so the whole task will abort
    handler.taskIterationAborted(capture(capturesForTaskID3[1]), anyObject());
    expectLastCall();
    
    // the whole task ID3 aborts...
    handler.taskAborted(eq(tasksById.get("ID3")), anyObject());
    expectLastCall();
    
    // ID3: iteration 2, which aborts because ID3 aborted
    handler.taskIterationStarted(capture(capturesForTaskID3[2]));
    expectLastCall();
    handler.taskIterationAborted(capture(capturesForTaskID3[3]), anyObject());
    expectLastCall();
    
    // task ID4 aborts because it depends on ID3
    handler.taskAborted(eq(tasksById.get("ID4")), anyObject());
    expectLastCall();
    
    replay(handler);
    
    compi.addTaskExecutionHandler(handler);
    compi.run();
    
    verify(handler);
    
    for (int i = 0; i < capturesForTaskID1.length; i++) {
		assertEquals("ID1", capturesForTaskID1[i].getValue().getId());
	}
	for (int i = 0; i < capturesForTaskID3.length; i++) {
		assertEquals("ID3", capturesForTaskID3[i].getValue().getId());
	}
	assertEquals("2", capturesForTaskID1[0].getValue().getIterationValue());
	assertEquals("2", capturesForTaskID1[1].getValue().getIterationValue());
	assertEquals("-errorj", capturesForTaskID3[0].getValue().getIterationValue());
  }
  
  @Test
  public void testSkipTasks() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSkipTasks.xml").getFile();
    final String fromTask = "ID3";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(3, handler.getStartedTasks().size());
    assertEquals(3, handler.getFinishedTasks().size());
    assertTrue(handler.getFinishedTasks().containsAll(Arrays.asList("ID3", "ID4", "ID2")));
  }

  @Test
  public void testSkipTaskWithLoops() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSkipTasksWithLoops.xml").getFile();

    final String fromTask = "ID4";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
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
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTheSingleTask(singleTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(1, handler.getStartedTasks().size());
    assertEquals(1, handler.getFinishedTasks().size());
  }

  @Test
  public void testRunUntilTask() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testRunUntilTask.xml").getFile();

    final String untilTask = "ID3";

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsUntilTask(untilTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
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
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .whichRunsUntilTask(untilTask)
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(2, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-3"));
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertEquals(2, handler.getFinishedTasks().size());
  }

  @Test
  public void testUntilStartingFroms() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String[] fromTasks = {
      "task-3", "task-7"
    };
    final String untilTask = "task-8";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTasks(asList(fromTasks))
          .whichRunsUntilTask(untilTask)
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(4, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-3"));
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertTrue(handler.getStartedTasks().contains("task-7"));
    assertTrue(handler.getStartedTasks().contains("task-8"));
    assertEquals(4, handler.getFinishedTasks().size());
  }

  @Test
  public void testUntilStartingAfters() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String[] afterTasks = {
      "task-3", "task-7"
    };
    final String untilTask = "task-8";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTasksAfterTasks(asList(afterTasks))
          .whichRunsUntilTask(untilTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(2, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertTrue(handler.getStartedTasks().contains("task-8"));
    assertEquals(2, handler.getFinishedTasks().size());
  }

  @Test
  public void testUntilStartingAfterAndFrom() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String afterTask = "task-7";
    final String fromTask = "task-3";
    final String untilTask = "task-8";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTasksAfterTask(afterTask)
          .whichStartsFromTask(fromTask)
          .whichRunsUntilTask(untilTask)
          .build()
      );

    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(3, handler.getStartedTasks().size());
    assertTrue(handler.getStartedTasks().contains("task-3"));
    assertTrue(handler.getStartedTasks().contains("task-6"));
    assertTrue(handler.getStartedTasks().contains("task-8"));
    assertEquals(3, handler.getFinishedTasks().size());
  }

  @Test
  public void testBeforeStartingFrom() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("pipelinePartialRuns.xml").getFile();

    final String fromTask = "task-3";
    final String beforeTask = "task-8";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .whichRunsTasksBeforeTask(beforeTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
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
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichStartsFromTask(fromTask)
          .whichRunsUntilTask(untilTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
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
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTasksBeforeTask(beforeTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
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

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(1, handler.getStartedTasks().size());
    assertEquals(0, handler.getFinishedTasks().size());
    assertEquals(4, handler.getAbortedTasks().size());
  }

  @Test
  public void testTasksAborted() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testTasksAborted.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(3, handler.getStartedTasks().size());
    assertEquals(2, handler.getFinishedTasks().size());
    assertEquals(2, handler.getAbortedTasks().size());
  }

  @Test
  public void testSingleTasksAborted() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testTasksAborted.xml").getFile();

    final String singleTask = "ID3";
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .whichRunsTheSingleTask(singleTask)
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(1, handler.getStartedTasks().size());
    assertEquals(0, handler.getFinishedTasks().size());
    assertEquals(1, handler.getAbortedTasks().size());
  }

  @Test
  public void testTasksAbortedWithLoops() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testTasksAbortedWithLoops.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();
    assertEquals(3, handler.getStartedTasks().size());

    assertEquals(5, handler.getFinishedTasksIncludingLoopChildren().size());
    assertEquals(2, handler.getAbortedTasks().size());
  }

  @Test
  public void testSomeTasksAbortedAndContinue() throws Exception {
    final String pipelineFile = ClassLoader.getSystemResource("testSomeTasksAbortedAndContinue.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(4, handler.getStartedTasks().size());
    assertEquals(3, handler.getFinishedTasks().size());
    assertEquals(2, handler.getAbortedTasks().size());
  }

  @Test
  public void testSomeTasksAbortedAndContinueWithLoops() throws Exception {
    final String pipelineFile =
      ClassLoader.getSystemResource("testSomeTasksAbortedAndContinueWithLoops.xml")
        .getFile();
    
    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)))
          .build()
      );
    
    TestExecutionHandler handler = new TestExecutionHandler();
    compi.addTaskExecutionHandler(handler);

    compi.run();

    assertEquals(4, handler.getStartedTasks().size());
    assertEquals(3, handler.getFinishedTasksIncludingLoopChildren().size());
    assertEquals(3, handler.getAbortedTasks().size());
  }

  private static class TestExecutionHandler implements TaskExecutionHandler {
    final List<String> startedTasks = new ArrayList<>();
    final List<String> finishedTasks = new ArrayList<>();
    final List<String> finishedTasksIncludingLoopChildren = new ArrayList<>();
    final List<String> abortedTasks = new ArrayList<>();
    final Set<String> startedForeachs = new HashSet<>();

    @Override
    synchronized public void taskStarted(Task task) {
      startedTasks.add(task.getId());
    }

    @Override
    public void taskFinished(Task task) {
      finishedTasks.add(task.getId());
      if (!(task instanceof Foreach)) finishedTasksIncludingLoopChildren.add(task.getId());
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
    
    @Override
    public void taskIterationStarted(ForeachIteration iteration) {
      startedForeachs.add(iteration.getParentForeachTask().getId());
      
    }

    @Override
    public void taskIterationFinished(ForeachIteration iteration) {
      finishedTasksIncludingLoopChildren.add(iteration.getParentForeachTask().getId());
    }

    @Override
    public void taskIterationAborted(ForeachIteration iteration, Exception e) {
    }
  }

}
