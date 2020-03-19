/*-
 * #%L
 * Compi Development Kit
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
package org.sing_group.compi.dk.cli;

import static java.util.Arrays.asList;
import static org.sing_group.compi.dk.CompiDKVersion.getCompiDKVersion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.sing_group.compi.dk.CompiDKException;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.CLIApplicationCommandException;
import es.uvigo.ei.sing.yacli.command.Command;

public class CompiDKCLI extends CLIApplication {

  private static final Logger LOGGER = Logger.getLogger(CompiDKCLI.class.getName());

  static {
    configureLog();
  }

  @Override
  protected List<Command> buildCommands() {
    return asList(
      new NewProjectCommand(),
      new BuildCommand(),
      new HubInitCommand(),
      new HubPushCommand(),
      new HubMetadataCommand()
    );
  }

  @Override
  protected String getApplicationName() {
    return "Compi Development Kit (version: " + getCompiDKVersion() + ")";
  }

  @Override
  protected String getApplicationVersion() {
    return getCompiDKVersion();
  }

  @Override
  protected String getApplicationCommand() {
    return "compi-dk";
  }

  private static void configureLog() {
    InputStream stream =
      CompiDKCLI.class.getClassLoader()
        .getResourceAsStream("logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(stream);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void handleCommandException(CLIApplicationCommandException exception, PrintStream out) {
    if (exception.getCause() instanceof CompiDKException) {
      throw (CompiDKException) exception.getCause();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      super.handleCommandException(exception, new PrintStream(baos));
    } catch (Exception e) {
      // e == exception
    }
    throw new CompiDKException(exception.getCause().getClass().getSimpleName(), baos.toString(), exception.getCause());
  }

  public static void main(String[] args) {
    try {
      new CompiDKCLI().run(args);
    } catch (CompiDKException compiException) {
      // compiException.printStackTrace();
      LOGGER.severe(compiException.getShortMessage());
      LOGGER.severe(compiException.getDisplayMessage());
      System.exit(1);
    } catch (Exception e) {
      // e.printStackTrace(); //TODO: add compi cli argument "-X" to show this
      System.exit(1);
    }
  }
}
