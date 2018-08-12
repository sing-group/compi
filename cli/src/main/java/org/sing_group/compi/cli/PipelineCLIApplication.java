package org.sing_group.compi.cli;

import static es.uvigo.ei.sing.yacli.command.CommandPrinter.printCommandOptionsExtended;
import static java.util.Arrays.asList;
import static org.sing_group.compi.cli.commands.RunSpecificPipelineCommand.newRunSpecificPipelineCommand;

import java.io.PrintStream;
import java.util.List;

import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.cli.commands.RunSpecificPipelineCommand;
import org.sing_group.compi.core.CompiApp;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;
import es.uvigo.ei.sing.yacli.command.option.Option;

public class PipelineCLIApplication extends CLIApplication {

	private static CompiApp compiApp;
	private static List<Option<?>> compiGeneralOptions;
	private static String[] commandLineArgs;

	private String pipelineName;

	public static PipelineCLIApplication newPipelineCLIApplication(
		String pipelineName, CompiApp compiApp,
		List<Option<?>> compiGeneralOptions, String[] commandLineArgs
	) {
		PipelineCLIApplication.compiApp = compiApp;
		PipelineCLIApplication.compiGeneralOptions = compiGeneralOptions;
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
	protected String getApplicationCommand() {
		return "compi.sh";
	}

	@Override
	protected List<Command> buildCommands() {
		return asList( 
			newRunSpecificPipelineCommand(compiApp, compiGeneralOptions, commandLineArgs)
		);
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
