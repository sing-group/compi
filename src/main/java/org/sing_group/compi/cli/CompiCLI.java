package org.sing_group.compi.cli;

import java.util.ArrayList;
import java.util.List;

import org.sing_group.compi.cli.commands.RunCommand;

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
public class CompiCLI extends CLIApplication {

	
	public static String[] args;

	public CompiCLI() {
		super(true, true, true);
	}
	/**
	 * Creates a command
	 */
	@Override
	protected List<Command> buildCommands() {
		final List<Command> commands = new ArrayList<>();
		commands.add(new RunCommand(args));
		return commands;
	}

	/**
	 * Getter of the application command
	 */
	@Override
	protected String getApplicationCommand() {
		return "compi.sh";
	}

	/**
	 * Getter of the application name
	 */
	@Override
	protected String getApplicationName() {
		return "Compi App";
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            Parameters received in the command line interface
	 */
	public static void main(final String[] args) {
		CompiCLI.args = args;
		new CompiCLI().run(args);
	}

}
