package org.sing_group.compi.dk.cli;

import java.util.Arrays;
import java.util.List;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;

public class CompiDKCLI extends CLIApplication {

	@Override
	protected List<Command> buildCommands() {
		return Arrays.asList(new NewProjectCommand(), new BuildCommand());
		
	}

	@Override
	protected String getApplicationName() {
		return "Compi Development Kit";
	}

	@Override
	protected String getApplicationCommand() {
		return "compi-dk.sh";
	}
	
	public static void main(String[] args) {
		new CompiDKCLI().run(args);
	}

}
