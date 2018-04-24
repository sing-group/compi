package org.sing_group.compi.cli.commands;

import static org.sing_group.compi.cli.PipelineCLIApplication.newPipelineCLIApplication;

import java.util.ArrayList;
import java.util.List;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.VariableResolver;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.IntegerOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunCommand extends AbstractCommand {

	private String[] commandLineArgs;
	private CompiApp compi;

	public RunCommand(String[] args) {
		this.commandLineArgs = args;
	}

	@Override
	public void execute(final Parameters parameters) throws Exception {

		System.out.println("Compi running with: ");
		System.out.println("Pipeline file - " + parameters.getSingleValue(super.getOption("p")));
		System.out.println("Number of threads - " + parameters.getSingleValue(super.getOption("t")));
		if (parameters.hasOption(super.getOption("pa"))) {
			System.out.println("Params file - " + parameters.getSingleValue(super.getOption("pa")));
		}

		if (parameters.getSingleValue(super.getOption("s")) != null
				&& parameters.getSingleValue(super.getOption("st")) != null) {
			throw new IllegalArgumentException("You can only specify skip or single-task, but not both: ");
		}

		if (parameters.getSingleValue(super.getOption("s")) != null) {
			System.out.println("Skip to task - " + parameters.getSingleValue(super.getOption("s")) + "\n");
		}
		if (parameters.getSingleValue(super.getOption("st")) != null) {
			System.out.println("Running single task - " + parameters.getSingleValue(super.getOption("st")) + "\n");
		}
		compi = new CompiApp(parameters.getSingleValue(super.getOption("p")),
				parameters.getSingleValue(super.getOption("t")), (VariableResolver) null,
				parameters.getSingleValue(super.getOption("s")), parameters.getSingleValue(super.getOption("st")));

		try {

			CLIApplication pipelineApplication = newPipelineCLIApplication(
					parameters.getSingleValue(super.getOption("p")), compi, this.createOptions());
			
			pipelineApplication.run(this.commandLineArgs);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.out.println("--Error--");
			System.out.println("Type - " + e.getClass());
			System.out.println("Message -  " + e.getMessage());
		}
	}

	/**
	 * Getter of the description
	 * 
	 * @return The description
	 */
	@Override
	public String getDescription() {
		return "Runs a pipeline";
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name
	 */
	@Override
	public String getName() {
		return "run";
	}

	@Override
	public String getDescriptiveName() {
		return "Run compi";
	}

	/**
	 * Returns a {@link List} with all the {@link Option}
	 * 
	 * @return A {@link List} with all the {@link Option}
	 */
	@Override
	protected List<Option<?>> createOptions() {
		final List<Option<?>> options = new ArrayList<>();
		options.add(new StringOption("pipeline", "p", "pipeline file", false, true, false));
		options.add(new StringOption("params", "pa", "params file", true, true, false));
		options.add(new IntegerOption("num-threads", "t", "number of threads to use", "6"));
		options.add(new DefaultValuedStringOption("skip", "s",
				"skip to task. Run the pipeline from the specific without running its dependencies. This option is incompatible with --single-task",
				null));
		options.add(new DefaultValuedStringOption("single-task", "st",
				"Runs a single task without its depencendies. This option is incompatible with --skip", null));

		return options;
	}

}