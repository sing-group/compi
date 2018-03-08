package org.sing_group.compi.cli.commands;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.cli.CompiCLI;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.ProgramExecutionHandler;
import org.sing_group.compi.core.VariableResolver;
import org.sing_group.compi.xmlio.PipelineParser;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Parameter;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.IntegerOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunCommand extends AbstractCommand {


		@Override
		public void execute(final Parameters parameters) throws Exception {
			
			System.out.println("Compi running with: ");
			System.out.println("Pipeline file - " + parameters.getSingleValue(super.getOption("p")));
			System.out.println("Number of threads - " + parameters.getSingleValue(super.getOption("t")));
			if (parameters.hasOption(super.getOption("pa"))) {
				System.out.println("Params file - " + parameters.getSingleValue(super.getOption("pa")));
			}

			
			if (parameters.getSingleValue(super.getOption("s"))!=null && parameters.getSingleValue(super.getOption("sp"))!=null) {
				throw new IllegalArgumentException("You can only specify skip or single-program, but not both: ");
			}
			
			if (parameters.getSingleValue(super.getOption("s"))!=null) {
				System.out.println("Skip to program - " + parameters.getSingleValue(super.getOption("s")) + "\n");
			}
			if (parameters.getSingleValue(super.getOption("sp"))!=null) {
				System.out.println("Running single program - " + parameters.getSingleValue(super.getOption("sp")) + "\n");
			}
			try {
				final CompiApp compi = new CompiApp(parameters.getSingleValue(super.getOption("p")));

				compi.addProgramExecutionHandler(new ProgramExecutionHandler() {

					@Override
					public void programStarted(Program program) {
						System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Program with id "
								+ program.getId() + " started");
					}

					@Override
					public void programFinished(Program program) {
						if (program.isSkipped()) {
							System.out.println("CLI - Program with id " + program.getId() + " skipped");
						} else {
							if (program.getForeach() != null) {
								System.out.println((System.currentTimeMillis() / 1000) + " - CLI - SubProgram with id "
										+ program.getId() + " finished - " + program.getToExecute());
							} else {
								System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Program with id "
										+ program.getId() + " finished");
							}
						}
					}

					@Override
					public void programAborted(Program program, Exception e) {
						System.out.println((System.currentTimeMillis() / 1000) + " - CLI - Program with id "
								+ program.getId() + " aborted - Cause - " + e.getClass());
					}

				});
				VariableResolver resolver = null;
				if (parameters.hasOption(super.getOption("pa"))) {
					resolver = new XMLParamsFileVariableResolver(parameters.getSingleValue(super
							.getOption
							("pa")));
				} else {
					resolver = new VariableResolver() {

						@Override
						public String resolveVariable(String variable) throws IllegalArgumentException {
							if (RunCommand.this.getOption(variable) == null) {
								throw new IllegalArgumentException(
										"The tag: \"" + variable + "\" doesn't exist in the arguments");
							}
							return parameters.getSingleValue(RunCommand.this.getOption(variable));
						}
						
					};
				}
				
				compi.run(parameters.getSingleValue(super.getOption("t")), resolver,
						parameters.getSingleValue(super.getOption("s")), parameters.getSingleValue(super.getOption("sp")));
			} catch (JAXBException | InterruptedException | SAXException | IOException | ParserConfigurationException
					| IllegalArgumentException e) {
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
			options.add(new DefaultValuedStringOption("skip", "s", "skip to program. Run the pipeline from the specific without running its dependencies. This option is incompatible with --single-program", null));
			options.add(new DefaultValuedStringOption("single-program", "sp", "Runs a single program without its depencendies. This option is incompatible with --skip", null));

			try {
				for (int i = 0; i < CompiCLI.args.length; i++){
					String arg = CompiCLI.args[i];
					if (arg.equals("--pipeline") || arg.equals("-p")){
						Pipeline p = PipelineParser.parsePipeline(new File(CompiCLI.args[i+1]));
						List<Parameter> params = p.getParams();	
						for (int j = 0; j < params.size(); j++){
							Parameter param = params.get(j);
							options.add(new StringOption(param.getName(), param.getShortName(), param.getDescription(),
									false, true, false));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return options;
		}

	}