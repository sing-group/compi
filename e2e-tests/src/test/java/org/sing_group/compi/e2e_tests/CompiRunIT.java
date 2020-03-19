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
package org.sing_group.compi.e2e_tests;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class CompiRunIT {

  private CommandResults runCompi(String... args) throws InterruptedException, ExecutionException {

    PrintStream stdOut = null;
    PrintStream stdErr = null;

    // debug: show outputs
    stdOut = System.out;
    stdErr = System.err;

    CommandResults results = CommandRunner.runCommand("../cli/target/dist/compi", stdOut, stdErr, args).get();

    return results;
  }

  @Test
  public void testEchoPipelineSuccess() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess =
      runCompi(
        "run", "-p", "../core/src/test/resources/echoPipeline.xml", "--", "--text",
        "hello", "--destination", "/tmp/test"
      );

    assertThat(compiProcess.getReturnValue(), is(0));
  }

  @Test
  public void testMissingParameters() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess = runCompi("run", "-p", "../core/src/test/resources/echoPipeline.xml");

    assertThat(compiProcess.getReturnValue(), is(not(0)));
    assertThat(compiProcess.getStdError(), containsString("Error parsing command: option destination is mandatory"));
  }

  @Test
  public void testInvalidPipeline() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess = runCompi("run", "-p", "../core/src/test/resources/pipelineInvalidParameterName.xml");

    assertThat(compiProcess.getReturnValue(), is(not(0)));
    assertThat(compiProcess.getStdError(), containsString("Pipeline is not valid"));
  }

  @Test
  public void testWarningPipeline() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess = runCompi("run", "-p", "../core/src/test/resources/pipelineNotParameterInSource.xml");

    assertThat(compiProcess.getReturnValue(), is(0));
    assertThat(compiProcess.getStdError(), containsString("WARNING"));
    assertThat(compiProcess.getStdError(), containsString("found in code"));
  }

  @Test
  public void testWarningPipelineAbort() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess =
      runCompi(
        "run", "-p", "../core/src/test/resources/pipelineNotParameterInSource.xml",
        "--abort-if-warnings"
      );

    assertThat(compiProcess.getReturnValue(), is(not(0)));
    assertThat(compiProcess.getStdError(), containsString("WARNING"));
    assertThat(compiProcess.getStdError(), containsString("found in code"));
    assertThat(compiProcess.getStdError(), containsString("Aborted due to warnings"));

  }

  @Test
  public void testExportGraph() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess =
      runCompi(
        "export-graph", "-p", "../core/src/test/resources/echoPipeline.xml", "-o",
        "/tmp/export-graph.png"
      );

    assertThat(compiProcess.getReturnValue(), is(0));

  }

  @Test
  public void testValidate() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess = runCompi("validate", "-p", "../core/src/test/resources/echoPipeline.xml");

    assertThat(compiProcess.getReturnValue(), is(0));
    assertThat(compiProcess.getStdError(), containsString("Pipeline file is OK."));
  }

  @Test
  public void testValidateInvalidPipeline() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess =
      runCompi(
        "Validate", "-p",
        "../core/src/test/resources/pipelineInvalidParameterName.xml"
      );

    assertThat(compiProcess.getReturnValue(), is(not(0)));
    assertThat(compiProcess.getStdError(), containsString("SEVERE"));
    assertThat(compiProcess.getStdError(), containsString("XML_SCHEMA_VALIDATION_ERROR"));
  }

  @Test
  public void testValidateInvalidRunnersFile() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess =
      runCompi(
        "run", "-p", "../core/src/test/resources/echoPipeline.xml", "-r", "does-not-exist", "--", "--text",
        "hello", "--destination", "/tmp/test"
      );

    assertThat(compiProcess.getReturnValue(), is(not(0)));
    assertThat(compiProcess.getStdError(), containsString("SEVERE"));
    assertThat(compiProcess.getStdError(), containsString("Runners file not found"));
  }
}