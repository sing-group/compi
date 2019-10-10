/*-
 * #%L
 * Compi CLI
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
package org.sing_group.compi.cli;

import static org.sing_group.compi.cli.commands.RunCommand.getCompiParameters;
import static org.sing_group.compi.core.CompiApp.getCompiVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.sing_group.compi.cli.commands.ExportGraphCommand;
import org.sing_group.compi.cli.commands.HelpTaskCommand;
import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.cli.commands.ValidatePipelineCommand;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;

/**
 * Contains application main method<br>
 * It has 4 parameters (2 mandatory and 2 optional):
 * <ul>
 * <li>1º pipeline xml file (mandatory)</li>
 * <li>2º params xml file (optional)</li>
 * <li>3º number of threads (mandatory)</li>
 * <li>4º the task ID where you want to start (If there is not a correct ID,
 * it will not skip any task). (optional)</li>
 * </ul>
 * Example:
 * 
 * @author Jesus Alvarez Casanova
 * @author Daniel Glez-Peña
 *
 */
public abstract class CompiCLI extends CLIApplication {

	static {
		configureLog();
	}

	public CompiCLI() {
		super(true, true, false);
	}

	@Override
	protected List<Command> buildCommands() {
		final List<Command> commands = new ArrayList<>();
		commands.add(new RunCommand(getCommandLineArgs()));
		commands.add(new ValidatePipelineCommand());
		commands.add(new ExportGraphCommand());
		commands.add(new HelpTaskCommand(getCommandLineArgs()));

		return commands;
	}

	@Override
	protected String getApplicationCommand() {
		return "compi";
	}

	@Override
	protected String getApplicationVersion() {
		return getCompiVersion();
	}

	@Override
	protected String getApplicationName() {
		return "Compi App (version " + getCompiVersion() + ")";
	}

	private static void configureLog() {
		InputStream stream = CompiCLI.class.getClassLoader()
			.getResourceAsStream("logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static CompiCLI newCompiCLI(String[] args) {
		return new CompiCLI() {
			String[] getCommandLineArgs() {
				return args;
			}
		};
	}

	abstract String[] getCommandLineArgs();

	public static void main(final String[] args) {
		CompiCLI compiCLI = newCompiCLI(args);

		compiCLI.run(getCompiParameters(args));
	}
}
