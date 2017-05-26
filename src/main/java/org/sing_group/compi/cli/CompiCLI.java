package org.sing_group.compi.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.xmlio.PipelineParser;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Parameter;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;
import org.xml.sax.SAXException;

import es.uvigo.ei.sing.yacli.AbstractCommand;
import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.Command;
import es.uvigo.ei.sing.yacli.Option;
import es.uvigo.ei.sing.yacli.Parameters;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.ProgramExecutionHandler;
import org.sing_group.compi.core.VariableResolver;

/**
 * Contains application main method<br>
 * It has 4 parameters (2 mandatory and 2 optional):
 * <ul>
 * <li>1º pipeline xml file (mandatory)</li>
 * <li>2º params xml file (optional)</li>
 * <li>3º number of threads (mandatory)</li>
 * <li>4º the program ID where you want to start (If there is not a correct ID,
 * it will not skip any program). (optional)</li>
 * </ul>
 * Example:
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class CompiCLI extends CLIApplication {

	
	private static String[] args;

	
	private class RunCommand extends AbstractCommand {

		@Override
		public void execute(final Parameters arg0) throws Exception {
			System.out.println("Compi running with: ");
			System.out.println("Pipeline file - " + arg0.getSingleValue(super.findOption("p")));
			System.out.println("Number of threads - " + arg0.getSingleValue(super.findOption("t")));
			System.out.println("Params file - " + arg0.getSingleValue(super.findOption("pa")));
			System.out.println("Skip to program - " + arg0.getSingleValue(super.findOption("s")) + "\n");
			try {
				final CompiApp compi = new CompiApp(arg0.getSingleValue(super.findOption("p")));

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
				if (arg0.getSingleValue(super.findOption("pa"))!=null) {
					resolver = new XMLParamsFileVariableResolver(arg0.getSingleValue(super.findOption("pa")));
				} else {
					resolver = new VariableResolver() {

						@Override
						public String resolveVariable(String variable) throws IllegalArgumentException {
							if (RunCommand.this.findOption(variable) == null) {
								throw new IllegalArgumentException(
										"The tag: \"" + variable + "\" doesn't exist in the arguments");
							}
							return arg0.getSingleValue(RunCommand.this.findOption(variable));							
						}
						
					};
				}
				
				compi.run(arg0.getSingleValue(super.findOption("t")), resolver,
						arg0.getSingleValue(super.findOption("s")));
			} catch (JAXBException | InterruptedException | SAXException | IOException | ParserConfigurationException
					| IllegalArgumentException e) {
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

		/**
		 * Returns a {@link List} with all the {@link Option}
		 * 
		 * @return A {@link List} with all the {@link Option}
		 */
		@Override
		protected List<Option> createOptions() {
			final List<Option> options = new ArrayList<>();
			options.add(new Option("pipeline", "p", "pipeline file", false, true, false));
			options.add(new Option("params", "pa", "params file", true, true, false));
			options.add(new Option("num-threads", "t", "number of threads to use", false, true, false));
			options.add(new Option("skip", "s", "skip to program", true, true, false));

			try {
				for (int i = 0; i < CompiCLI.args.length; i++){
					String arg = CompiCLI.args[i];
					if (arg.equals("--pipeline") || arg.equals("-p")){
						Pipeline p = PipelineParser.parsePipeline(new File(CompiCLI.args[i+1]));
						List<Parameter> params = p.getParams();	
						for (int j = 0; j < params.size(); j++){
							Parameter param = params.get(j);
							options.add(new Option(param.getName(), param.getShortName(), param.getDescription(), false, true, false));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return options;
		}

	}

	/**
	 * Creates a command
	 */
	@Override
	protected List<Command> buildCommands() {
		final List<Command> commands = new ArrayList<>();
		commands.add(new RunCommand());
		return commands;
	}

	/**
	 * Getter of the application command
	 */
	@Override
	protected String getApplicationCommand() {
		return "[compi.sh|compi.bat]";
	}

	/**
	 * Getter of the application name
	 */
	@Override
	protected String getApplicationName() {
		return "Compi App";
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            Parameters received in the command line interface
	 */
	public static void main(final String[] args) {
		CompiCLI.args = args;
		new CompiCLI().run(args);
	}

}
