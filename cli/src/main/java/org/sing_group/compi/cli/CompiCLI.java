package org.sing_group.compi.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;

import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.cli.commands.ValidatePipelineCommand;
import org.sing_group.compi.core.CompiApp;

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

  static {
    configureLog();
  }
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
		commands.add(new ValidatePipelineCommand());
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
		return "Compi App (version "+CompiApp.getCompiVersion()+")";
	}

  private static void configureLog() {
    InputStream stream = CompiCLI.class.getClassLoader().
            getResourceAsStream("logging.properties");
    try {
        LogManager.getLogManager().readConfiguration(stream);

    } catch (IOException e) {
        e.printStackTrace();
    }
  }
  
	/**
	 * Main method
	 * 
	 * @param args
	 *            Parameters received in the command line interface
	 */
	public static void main(final String[] args) {
		CompiCLI.args = args; //all args
		
		int indexOfParameterSeparator = Arrays.asList(args).indexOf("--");
		
		// before --
		String[] compiParameters = args;
		if (indexOfParameterSeparator > 0) {
		  compiParameters = new String[indexOfParameterSeparator];
		  System.arraycopy(args, 0, compiParameters, 0, indexOfParameterSeparator);
		}
		
		new CompiCLI().run(compiParameters);
	}

}
