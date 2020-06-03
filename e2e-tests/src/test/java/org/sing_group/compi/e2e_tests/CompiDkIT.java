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
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class CompiDkIT {

  private CommandResults runCompiDK(String... args) throws InterruptedException, ExecutionException {

    PrintStream stdOut = null;
    PrintStream stdErr = null;

    // debug: show outputs
    stdOut = System.out;
    stdErr = System.err;

    CommandResults results = CommandRunner.runCommand("../dk/target/dist/compi-dk", stdOut, stdErr, args).get();

    return results;
  }

  @Test
  public void testNewProjectSuccess() throws IOException, InterruptedException, ExecutionException {
    File dir =
      new File(
        System.getProperty("java.io.tmpdir") + File.separator + "compi-dk-e2e-project-" + UUID.randomUUID().toString()
      );

    CommandResults compiProcess = runCompiDK("new-project", "-p", dir.toString(), "-n", "my-test-project");

    assertThat(compiProcess.getReturnValue(), is(0));
    assertThat("Dockerfile", is(in((dir.list()))));
  }

  @Test
  public void testNewProjectFail() throws IOException, InterruptedException, ExecutionException {
    CommandResults compiProcess = runCompiDK("new-project");

    assertThat(compiProcess.getReturnValue(), is(1));
    assertThat(compiProcess.getStdError(), containsString("option image-name is mandatory"));
  }

  @Test
  public void testCreateAndBuild() throws IOException, InterruptedException, ExecutionException {
    File dir =
      new File(
        System.getProperty("java.io.tmpdir") + File.separator + "compi-dk-e2e-project-" + UUID.randomUUID().toString()
      );

    runCompiDK("new-project", "-p", dir.toString(), "-n", "my-test-project");

    CommandResults compiProcess = runCompiDK("build", "-p", dir.toString());

    assertThat(compiProcess.getReturnValue(), is(0));
  }

}