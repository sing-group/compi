package org.sing_group.compi.cli;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static org.sing_group.compi.core.CompiApp.getCompiVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import org.sing_group.compi.cli.commands.ExportGraphCommand;
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

		return commands;
	}

	@Override
	protected String getApplicationCommand() {
		return "compi";
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

		int indexOfParameterSeparator = asList(args).indexOf("--");

		// before --
		String[] compiParameters = args;
		if (indexOfParameterSeparator > 0) {
			compiParameters = new String[indexOfParameterSeparator];
			arraycopy(args, 0, compiParameters, 0, indexOfParameterSeparator);
		}

		compiCLI.run(compiParameters);
	}
}
