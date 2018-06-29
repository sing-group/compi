package org.sing_group.compi.cli;

import static java.util.Arrays.asList;

import java.util.List;

import org.sing_group.compi.cli.commands.RunSpecificPipelineCommand;
import org.sing_group.compi.core.CompiApp;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.Command;
import es.uvigo.ei.sing.yacli.command.option.Option;

public class PipelineCLIApplication extends CLIApplication {

	private static CompiApp compiApp;
	private static List<Option<?>> compiGeneralOptions;
	private String pipelineName;

	private PipelineCLIApplication() {
	}

	public static PipelineCLIApplication newPipelineCLIApplication(String pipelineName, CompiApp compiApp,
			List<Option<?>> compiGeneralOptions) {
		PipelineCLIApplication.compiApp = compiApp;
		PipelineCLIApplication.compiGeneralOptions = compiGeneralOptions;

		PipelineCLIApplication app = new PipelineCLIApplication(pipelineName);

		return app;
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
		return "compi.sh ";
	}

	@Override
	protected List<Command> buildCommands() {
		return asList( RunSpecificPipelineCommand.newRunSpecificPipelineCommand(compiApp, compiGeneralOptions));
	}

}
