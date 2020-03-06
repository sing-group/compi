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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.core.pipeline.Pipeline.fromFile;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.CompiRunConfiguration;

public class ResumeTest {

  @Test
  public void testResume() throws Exception {
    final File pipelineFile = new File(ClassLoader.getSystemResource("testResume.xml").getFile());
    final CompiRunConfiguration config = forPipeline(fromFile(pipelineFile), pipelineFile).build();

    final CompiApp compi = new CompiApp(config);

    TestExecutionHandler handler = new TestExecutionHandler();

    compi.addTaskExecutionHandler(handler);

    new Thread(
      () -> {
        try {
          Thread.sleep(1000);
          compi.requestStop();
        } catch (InterruptedException e) {}
      }
    ).start();

    compi.run();

    assertEquals(2, handler.getStartedTasks().size());
    assertEquals(1, handler.getFinishedTasks().size());
    assertEquals(1, compi.getExecutionLog().getFinishedTasks().size());
    assertFalse(handler.getFinishedTasks().contains("ID-2"));

    final CompiApp compi2 = new CompiApp(pipelineFile); // resume

    TestExecutionHandler handler2 = new TestExecutionHandler();
    compi2.addTaskExecutionHandler(handler2);

    compi2.run();

    assertEquals(1, handler2.getStartedTasks().size());
    assertEquals(1, handler2.getFinishedTasks().size());
    assertTrue(handler2.getFinishedTasks().contains("ID-2"));

  }

  @Test
  public void testResumeForeach() throws Exception {
    final File pipelineFile = new File(ClassLoader.getSystemResource("testResumeForeach.xml").getFile());
    final CompiRunConfiguration config = forPipeline(fromFile(pipelineFile), pipelineFile).build();

    final CompiApp compi = new CompiApp(config);

    TestExecutionHandler handler = new TestExecutionHandler();

    compi.addTaskExecutionHandler(handler);

    new Thread(
      () -> {
        try {
          Thread.sleep(1000);
          compi.requestStop();
        } catch (InterruptedException e) {}
      }
    ).start();

    compi.run();

    assertEquals(3, handler.getLoopIterations().stream().filter(it -> it.startsWith("S_")).count());
    assertEquals(2, handler.getLoopIterations().stream().filter(it -> it.startsWith("E_")).count());
    assertEquals(1, handler.getLoopIterations().stream().filter(it -> it.startsWith("A_")).count());

    final CompiApp compi2 = new CompiApp(pipelineFile); // resume

    TestExecutionHandler handler2 = new TestExecutionHandler();
    compi2.addTaskExecutionHandler(handler2);

    compi2.run();

    assertEquals(1, handler2.getLoopIterations().stream().filter(it -> it.startsWith("S_")).count());
    assertEquals(1, handler2.getLoopIterations().stream().filter(it -> it.startsWith("E_")).count());
    assertEquals(0, handler2.getLoopIterations().stream().filter(it -> it.startsWith("A_")).count());

    assertTrue(handler2.getLoopIterations().contains("S_ID-1_2"));
    assertTrue(handler2.getLoopIterations().contains("E_ID-1_2"));

  }

  @Test
  public void testResumeIterationBindedForeach() throws Exception {
    final File pipelineFile = new File(ClassLoader.getSystemResource("testResumeIterationBindedForeach.xml").getFile());
    final CompiRunConfiguration config = forPipeline(fromFile(pipelineFile), pipelineFile).build();

    final CompiApp compi = new CompiApp(config);

    TestExecutionHandler handler = new TestExecutionHandler();

    compi.addTaskExecutionHandler(handler);

    new Thread(
      () -> {
        try {
          Thread.sleep(1000);
          compi.requestStop();
        } catch (InterruptedException e) {}
      }
    ).start();

    compi.run();

    assertEquals(5, handler.getLoopIterations().stream().filter(it -> it.startsWith("S_")).count());
    assertEquals(4, handler.getLoopIterations().stream().filter(it -> it.startsWith("E_")).count());
    assertEquals(2, handler.getLoopIterations().stream().filter(it -> it.startsWith("A_")).count());

    final CompiApp compi2 = new CompiApp(pipelineFile); // resume

    TestExecutionHandler handler2 = new TestExecutionHandler();
    compi2.addTaskExecutionHandler(handler2);

    compi2.run();

    assertEquals(2, handler2.getLoopIterations().stream().filter(it -> it.startsWith("S_")).count());
    assertEquals(2, handler2.getLoopIterations().stream().filter(it -> it.startsWith("E_")).count());
    assertEquals(0, handler2.getLoopIterations().stream().filter(it -> it.startsWith("A_")).count());

    assertTrue(handler2.getLoopIterations().contains("S_ID-1_2"));
    assertTrue(handler2.getLoopIterations().contains("E_ID-1_2"));
    assertTrue(handler2.getLoopIterations().contains("S_ID-2_2"));
    assertTrue(handler2.getLoopIterations().contains("E_ID-2_2"));
  }

  @Test(expected = RuntimeException.class)
  public void testResumeStrict() throws Exception {
    final File pipelineFile = new File(ClassLoader.getSystemResource("testResume.xml").getFile());

    final CompiRunConfiguration config = forPipeline(fromFile(pipelineFile), pipelineFile).build();

    final CompiApp compi = new CompiApp(config);

    TestExecutionHandler handler = new TestExecutionHandler();

    compi.addTaskExecutionHandler(handler);

    new Thread(
      () -> {
        try {
          Thread.sleep(1000);
          compi.requestStop();
        } catch (InterruptedException e) {}
      }
    ).start();

    compi.run();

    byte[] original = FileUtils.readFileToByteArray(pipelineFile);
    byte[] finalContents = new byte[original.length + 1];
    System.arraycopy(original, 0, finalContents, 0, original.length);
    finalContents[finalContents.length - 1] = '\n';

    FileUtils.writeByteArrayToFile(pipelineFile, finalContents);

    new CompiApp(pipelineFile); // resume
  }
}
