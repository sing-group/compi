package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;
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
  private static String[] commandLineArgs;

  public static RunSpecificPipelineCommand newRunSpecificPipelineCommand(
    CompiApp compiApp, List<Option<?>> compiGeneralOptions, String[] commandLineArgs
  ) {
    RunSpecificPipelineCommand.compiApp = compiApp;
    RunSpecificPipelineCommand.commandLineArgs = commandLineArgs;

    return new RunSpecificPipelineCommand();
  }

  private RunSpecificPipelineCommand() {}

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

    VariableResolver pipelineVariableResolver = createPipelineVariableResolver(parameters);

    compiApp.setResolver(pipelineVariableResolver);

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

  private VariableResolver createPipelineVariableResolver(Parameters parameters) {
    Map<String, String> variables = new HashMap<>();

    // options defined as CLI parameters
    RunSpecificPipelineCommand.this.getOptions().forEach((option) -> {
      if (
        parameters.hasOption(option) && (!(option instanceof DefaultValuedOption) ||
          !((DefaultValuedOption<?>) option).getDefaultValue().equals(parameters.getSingleValue(option)))
      ) {
        variables.put(option.getParamName(), parameters.getSingleValueString(option));
      }
    });

    // options in the XML parameters file that have not been defined yet

    VariableResolver xmlResolver = getParamsFileResolver();
    if (xmlResolver != null) {
      xmlResolver.getVariableNames().forEach(variable -> {
        if (!variables.containsKey(variable)) {
          variables.put(variable, xmlResolver.resolveVariable(variable));
        }
      });
    }

    // default values of options that have not been defined yet
    RunSpecificPipelineCommand.this.getOptions().forEach((option) -> {
      if ((option instanceof DefaultValuedOption && !variables.containsKey(option.getParamName()))) {
        variables.put(option.getParamName(), ((DefaultValuedOption<?>) option).getDefaultValue());
      }
    });

    VariableResolver pipelineVariableResolver = new MapVariableResolver(variables);
    return pipelineVariableResolver;
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
    for (int i = 0; i < commandLineArgs.length; i++) {
      String arg = commandLineArgs[i];
      if (arg.equals("--params") || arg.equals("-pa")) {
        resolver =
          new XMLParamsFileVariableResolver(
            commandLineArgs[i + 1]
          );
      }
    }

    return resolver;
  }
}