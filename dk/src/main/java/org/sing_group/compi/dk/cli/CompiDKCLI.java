package org.sing_group.compi.dk.cli;

import static java.util.Arrays.asList;
import static org.sing_group.compi.dk.CompiDKVersion.getCompiDKVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

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
			new BuildCommand()
		);
	}

	@Override
	protected String getApplicationName() {
		return "Compi Development Kit (version: " + getCompiDKVersion() + ")";
	}

	@Override
	protected String getApplicationCommand() {
		return "compi-dk";
	}

	private static void configureLog() {
		InputStream stream = CompiDKCLI.class.getClassLoader()
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
