package org.sing_group.compi.cli.commands;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.sing_group.compi.cli.CompiCLI;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.VariableResolver;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.ParameterDescription;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.OptionCategory;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunSpecificPipelineCommand extends AbstractCommand {

	private static CompiApp compiApp;
	private static List<Option<?>> compiGeneralOptions;

	
	public static RunSpecificPipelineCommand newRunSpecificPipelineCommand(CompiApp compiApp, List<Option<?>> compiGeneralOptions) {
		RunSpecificPipelineCommand.compiApp = compiApp;
		RunSpecificPipelineCommand.compiGeneralOptions = compiGeneralOptions;
		
		return new RunSpecificPipelineCommand();
	}
	
	private RunSpecificPipelineCommand() {
	}

	@Override
	public String getName() {
		return "run";
	}

	@Override
	public String getDescriptiveName() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public void execute(Parameters parameters) throws Exception {
		/**
		 * A variable resolver that resolves variables by first looking at the command
		 * line parameters and then in the xml params file
		 * 
		 * @author lipido
		 *
		 */
		class PipelineVariableResolver implements VariableResolver {
			private XMLParamsFileVariableResolver xmlResolver;
			private Parameters parameters;

			public PipelineVariableResolver(final Parameters parameters) {
				this.parameters = parameters;
				if (parameters.hasOption(RunSpecificPipelineCommand.super.getOption("pa"))) {
					xmlResolver = new XMLParamsFileVariableResolver(
							parameters.getSingleValue(RunSpecificPipelineCommand.super.getOption("pa")));
				}
			}

			@Override
			public String resolveVariable(String variable) throws IllegalArgumentException {
				if (RunSpecificPipelineCommand.this.getOption(variable) != null) {
					return parameters.getSingleValue(RunSpecificPipelineCommand.this.getOption(variable));
				} else if (xmlResolver != null) {
					return xmlResolver.resolveVariable(variable);
				} else {
					return null;
				}
			}
		}

		compiApp.setResolver(new PipelineVariableResolver(parameters));

		compiApp.addTaskExecutionHandler(new TaskExecutionHandler() {

			@Override
			public void taskStarted(Task task) {
				System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Task with id " + task.getId()
						+ " started");
			}

			@Override
			public void taskFinished(Task task) {
				if (task.isSkipped()) {
					System.out.println("CLI - Task with id " + task.getId() + " skipped");
				} else {
					if (task instanceof Foreach) {
						System.out.println((System.currentTimeMillis() / 1000) + " - CLI - SubTask with id "
								+ task.getId() + " finished - " + task.getToExecute());
					} else {
						System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Task with id "
								+ task.getId() + " finished");
					}
				}
			}

			@Override
			public void taskAborted(Task task, Exception e) {
				System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Task with id " + task.getId()
						+ " aborted - Cause - " + e.getClass());
			}

		});
		compiApp.run();
	}

	@Override
	protected List<Option<?>> createOptions() {
		List<Option<?>> options = compiGeneralOptions;
		try {
			// find params file if it is available
			for (int i = 0; i < CompiCLI.args.length; i++) {
				String arg = CompiCLI.args[i];
				if (arg.equals("--pipeline") || arg.equals("-p")) {
					Pipeline p = compiApp.getPipeline();

					List<ParameterDescription> params = p.getParameterDescriptions();
					for (int j = 0; j < params.size(); j++) {
						ParameterDescription param = params.get(j);
						options.add(new StringOption(param.getName(), param.getShortName(), param.getDescription(),
								false, true, false));
					}

					p.getTasksByParameter().forEach((parameterName, tasks) -> {
						XMLParamsFileVariableResolver paramsFileResolver = getParamsFileResolver();
						ParameterDescription description = p.getParameterDescription(parameterName);
						List<OptionCategory> categories = tasks.stream().filter(task -> !task.isSkipped())
								.map(task -> new OptionCategory(task.getId())).collect(toList());

						if (categories.size() > 0) {
							if (description != null) {

								if (categories.size() > 0) {
									options.add(new StringOption(categories, description.getName(),
											description.getShortName(), description.getDescription(),
											paramsFileResolver != null
													&& paramsFileResolver.resolveVariable(parameterName) != null ? true
															: false,
											true, false));
								}
							} else {
								System.err.println("warning: description for parameter " + parameterName
										+ " not found in the pipeline XML file");
								options.add(new StringOption(categories, parameterName, parameterName, "",
										paramsFileResolver != null
												&& paramsFileResolver.resolveVariable(parameterName) != null ? true
														: false,
										true, false));
							}
						}

					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return options;
	}

	private XMLParamsFileVariableResolver getParamsFileResolver() {
		XMLParamsFileVariableResolver resolver = null;
		for (int i = 0; i < CompiCLI.args.length; i++) {
			String arg = CompiCLI.args[i];
			if (arg.equals("--params") || arg.equals("-pa")) {
				resolver = new XMLParamsFileVariableResolver(CompiCLI.args[i + 1]);
			}
		}
		return resolver;
	}

}