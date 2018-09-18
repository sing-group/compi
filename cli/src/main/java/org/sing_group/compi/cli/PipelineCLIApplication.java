package org.sing_group.compi.cli;

import static es.uvigo.ei.sing.yacli.command.CommandPrinter.printCommandOptionsExtended;
import static java.util.Arrays.asList;
import static org.sing_group.compi.cli.commands.RunSpecificPipelineCommand.newRunSpecificPipelineCommand;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.cli.commands.RunSpecificPipelineCommand;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.CompiRunConfiguration;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;

public class PipelineCLIApplication extends CLIApplication {

	private static CompiRunConfiguration config;
	private static String[] commandLineArgs;

	private String pipelineName;

	public static PipelineCLIApplication newPipelineCLIApplication(
	  String pipelineName,
	  CompiRunConfiguration config,
		String[] commandLineArgs
	) {
	  
		PipelineCLIApplication.config = config;
		PipelineCLIApplication.commandLineArgs = commandLineArgs;

		return new PipelineCLIApplication(pipelineName);
	}

	private PipelineCLIApplication(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	@Override
	protected String getApplicationName() {
		return "Compi - pipeline: " + this.pipelineName;
	}

	@Override
	protected String getApplicationVersion() {
		return CompiApp.getCompiVersion();
	}

	@Override
	protected String getApplicationCommand() {
		return "compi";
	}

	@Override
	protected List<Command> buildCommands() {
		try {
      return asList( 
      	newRunSpecificPipelineCommand(config, commandLineArgs)
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
	}

	@Override
	protected void printCommandHelp(Command command, PrintStream out) {
		if (command.getName().equals(RunSpecificPipelineCommand.NAME)) {
			super.printCommandUsageLine(command, out);
			out.println(" <general-options> -- <pipeline-parameters>");
			out.print("  where <general-options>: ");
			super.printCommandOptions(new RunCommand(commandLineArgs), out);
			out.println();
			printCommandOptionsExtended(new RunCommand(commandLineArgs), out);
			out.println();
			out.print("  where <pipeline-parameters>:");
			super.printCommandOptions(command, out);
			out.println();
			printCommandOptionsExtended(command, out);
		} else {
			super.printCommandHelp(command, out);
		}
	}
}
