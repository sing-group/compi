package org.sing_group.compi.dk.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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

  private static void configureLog() {
    InputStream stream = CompiDKCLI.class.getClassLoader().
            getResourceAsStream("logging.properties");
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
