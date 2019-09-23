/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.dk.cli;

import static java.util.Arrays.asList;
import static org.sing_group.compi.dk.CompiDKVersion.getCompiDKVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

import org.slf4j.LoggerFactory;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;

public class CompiDKCLI extends CLIApplication {

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
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("org.apache.http"))
      .setLevel(ch.qos.logback.classic.Level.WARN);

    InputStream stream =
      CompiDKCLI.class.getClassLoader()
        .getResourceAsStream("logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(stream);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

	public static void main(String[] args) {
		new CompiDKCLI().run(args);
	}
}
