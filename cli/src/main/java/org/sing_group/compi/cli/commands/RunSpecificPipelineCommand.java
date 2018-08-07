package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedOption;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.OptionCategory;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunSpecificPipelineCommand extends AbstractCommand {
	private static final Logger LOGGER = getLogger(RunSpecificPipelineCommand.class.getName());

	public static final String NAME = "run";

	private static CompiApp compiApp;

	public static RunSpecificPipelineCommand newRunSpecificPipelineCommand(
		CompiApp compiApp, List<Option<?>> compiGeneralOptions) {
		RunSpecificPipelineCommand.compiApp = compiApp;

		return new RunSpecificPipelineCommand();
	}

	private RunSpecificPipelineCommand() {
	}

	@Override
	public String getName() {
		return NAME;
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
     * A variable resolver that resolves variables by first looking at the
     * command line parameters and then in the xml params file
     * 
     * @author lipido
     *
     */
		class PipelineVariableResolver implements VariableResolver {
			private XMLParamsFileVariableResolver xmlResolver;
			private Parameters parameters;

			public PipelineVariableResolver(final Parameters parameters) {
				this.parameters = parameters;
				this.xmlResolver = getParamsFileResolver();
			}
	
	      @Override
	      public String resolveVariable(String variable) throws IllegalArgumentException {
	        if (
	          RunSpecificPipelineCommand.this.getOption(variable) != null
	            && parameters.hasOption(RunSpecificPipelineCommand.this.getOption(variable))
	            && (!(RunSpecificPipelineCommand.this.getOption(variable) instanceof DefaultValuedOption) ||
	              !((DefaultValuedOption<?>) RunSpecificPipelineCommand.this.getOption(variable)).getDefaultValue().equals(parameters.getSingleValue(RunSpecificPipelineCommand.this.getOption(variable))))
	        ) {
	          return parameters.getSingleValue(RunSpecificPipelineCommand.this.getOption(variable));
	        } else if (xmlResolver != null && xmlResolver.resolveVariable(variable) != null) {
	          return xmlResolver.resolveVariable(variable);
	        } else if (RunSpecificPipelineCommand.this.getOption(variable) instanceof DefaultValuedOption) {
	          return ((DefaultValuedOption<?>) RunSpecificPipelineCommand.this.getOption(variable)).getDefaultValue()
	            .toString();
	        } else {
	          return null;
	        }
	      }
	    }

		compiApp.setResolver(new PipelineVariableResolver(parameters));

		compiApp.addTaskExecutionHandler(new TaskExecutionHandler() {

			private Set<String> startedForeachs = new HashSet<String>();

			@Override
	      synchronized public void taskStarted(Task task) {
	        if (task instanceof Foreach) {
	          if (!startedForeachs.contains(task.getId())) {
	            LOGGER.info(
	              "> Started loop task " + task.getId() + " (command: " + task.getToExecute() + ") (stdout log: "
	                + (task.getFileLog() == null ? "none" : task.getFileLog()) + ", stderr log: "
	                + (task.getFileErrorLog() == null ? "none" : task.getFileErrorLog()) + ")"
	            );
	            startedForeachs.add(task.getId());
	          }
	          LOGGER.info(
	            ">> Started loop iteration of task " + task.getId() + " (command: " + task.getToExecute()
	              + ") (stdout log: " + (task.getFileLog() == null ? "none" : task.getFileLog()) + ", stderr log: "
	              + (task.getFileErrorLog() == null ? "none" : task.getFileErrorLog()) + ")"
	          );
	        } else {
	          LOGGER.info("> Started task " + task.getId() + " (command: " + task.getToExecute() + ")");
	        }
	      }
	
	      @Override
	      synchronized public void taskFinished(Task task) {
	        if (task.isSkipped()) {
	          LOGGER.fine("Task with id " + task.getId() + " skipped");
	        } else {
	          if (task instanceof Foreach) {
	            LOGGER.info(
	              "<< Finished loop iteration of task " + task.getId() + " (command: " + task.getToExecute()
	                + ")"
	            );
	            if (compiApp.getParentTask().get(task).isFinished()) {
	              LOGGER.info(
	                "< Finished loop task " + task.getId() + " (command: " + task.getToExecute()
	                  + ")"
	              );
	            }
	          } else {
	            LOGGER.info("< Finished task " + task.getId() + " (command: " + task.getToExecute() + ")");
	          }
	        }
	      }
	
	      @Override
	      synchronized public void taskAborted(Task task, Exception e) {
	        LOGGER.severe(
	          "X Aborted task " + task.getId() + " (command: " + task.getToExecute() + ") Cause - "
	            + e.getClass() + ": " + e.getMessage()
	        );
	      }
	
	    });
	    compiApp.run();
	  }
	
	  @Override
	  protected List<Option<?>> createOptions() {
	    List<Option<?>> options = /* compiGeneralOptions; */ new ArrayList<>();
	    try {
	      // find params file if it is available
	      /*
	       * for (int i = 0; i < CompiCLI.args.length; i++) { String arg =
	       * CompiCLI.args[i]; if (arg.equals("--pipeline") || arg.equals("-p")) {
	       */
	      Pipeline p = compiApp.getPipeline();
	
	      p.getTasksByParameter().forEach((parameterName, tasks) -> {
	        XMLParamsFileVariableResolver paramsFileResolver = getParamsFileResolver();
	        ParameterDescription description = p.getParameterDescription(parameterName);
	        List<OptionCategory> categories =
	          tasks.stream().filter(task -> !task.isSkipped())
	            .map(task -> new OptionCategory(task.getId())).collect(toList());
	
	        if (categories.size() > 0) {
	          if (description != null) {
	
	            if (categories.size() > 0) {
	              Option<String> option = null;
	              if (description.getDefaultValue() != null) {
	                option =
	                  new DefaultValuedStringOption(
	                    categories, description.getName(),
	                    description.getShortName(), description.getDescription(),
	                    description.getDefaultValue()
	                  );
	              } else {
	                option =
	                  new StringOption(
	                    categories, description.getName(),
	                    description.getShortName(), description.getDescription(),
	                    paramsFileResolver != null
	                      && paramsFileResolver.resolveVariable(parameterName) != null ? true
	                        : false,
	                    true, false
	                  );
	              }
	              options.add(option);
	            }
	          } else {
	            options.add(
	              new StringOption(
	                categories, parameterName, parameterName, "",
	                paramsFileResolver != null
	                  && paramsFileResolver.resolveVariable(parameterName) != null ? true
	                    : false,
	                true, false
	              )
	            );
	          }
	        }
	
	      });
	      /*
	       * } }
	       */
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
				System.err.println("creating resolver in file " + CompiCLI.args[i + 1]);
				resolver = new XMLParamsFileVariableResolver(
					CompiCLI.args[i + 1]);
			}
		}

		return resolver;
	}
}