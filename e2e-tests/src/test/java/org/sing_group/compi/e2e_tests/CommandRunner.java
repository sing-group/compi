/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2020 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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

import static org.apache.commons.io.IOUtils.copy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class CommandRunner implements Callable<CommandResults> {

  private String command;
  private String[] args;
  private PrintStream stdErr;
  private PrintStream stdOut;

  private CommandRunner(String command, String... args) {
    this(command, null, null, args);
  }

  private CommandRunner(String command, PrintStream stdOut, PrintStream stdErr, String... args) {
    this.command = command;
    this.args = args;
    this.stdOut = stdOut;
    this.stdErr = stdErr;
  }

  public static Future<CommandResults> runCommand(String command, String... args) {
    return runCommand(command, null, null, args);
  }

  public static Future<CommandResults> runCommand(
    String command, PrintStream stdOut, PrintStream stdErr, String... args
  ) {
    FutureTask<CommandResults> future = new FutureTask<>(new CommandRunner(command, stdOut, stdErr, args));
    Executors.newCachedThreadPool().execute(future);
    return future;
  }

  public CommandResults call() throws Exception {
    List<String> parameters = new LinkedList<>();

    parameters.add(command);

    parameters.addAll(Arrays.asList(args));
    ProcessBuilder pb = new ProcessBuilder().command(parameters);
    Process process = pb.start();

    OutputStream stdOutBaos = new ByteArrayOutputStream();
    Thread outThread = redirect(process.getInputStream(), tee(stdOut, stdOutBaos));

    ByteArrayOutputStream stdErrBaos = new ByteArrayOutputStream();
    Thread errThread = redirect(process.getErrorStream(), tee(stdErr, stdErrBaos));

    outThread.start();
    errThread.start();

    int retValue = process.waitFor();

    outThread.join();
    errThread.join();

    return new CommandResults() {

      @Override
      public String getStdOutput() {
        return stdOutBaos.toString();
      }

      @Override
      public String getStdError() {
        return stdErrBaos.toString();
      }

      @Override
      public int getReturnValue() {
        return retValue;
      }
    };
  }

  private Thread redirect(InputStream inputStream, OutputStream outputStream) {
    Thread t = new Thread(() -> {
      try {
        copy(inputStream, outputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    return t;
  }

  private OutputStream tee(OutputStream outA, OutputStream outB) {
    return new OutputStream() {

      @Override
      public void write(int b) throws IOException {
        if (outA != null)
          outA.write(b);
        if (outB != null)
          outB.write(b);
      }
    };
  }

}
