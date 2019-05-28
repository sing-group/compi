/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.cli.commands;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.CompiRunConfiguration;
import org.sing_group.compi.core.CompiTaskAbortedException;
import org.sing_group.compi.core.TaskExecutionHandler;
import org.sing_group.compi.core.loops.ForeachIteration;
import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.ParameterDescription;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;
import org.sing_group.compi.core.resolver.MapVariableResolver;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.ParamsFileVariableResolver;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedOption;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.OptionCategory;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class RunSpecificPipelineCommand extends AbstractCommand {
  private static final Logger LOGGER = getLogger(RunSpecificPipelineCommand.class.getName());

  public static final String NAME = "run";

  private static CompiRunConfiguration config;
  private static String[] commandLineArgs;
  private static CompiApp compiApp;
  private static VariableResolver resolver = null;

  public static RunSpecificPipelineCommand newRunSpecificPipelineCommand(
    CompiRunConfiguration config, String[] commandLineArgs
  ) throws IOException {

    config.setResolver(createPipelineVariableResolverProxy());

    RunSpecificPipelineCommand.config = config;
    RunSpecificPipelineCommand.commandLineArgs = commandLineArgs;
    RunSpecificPipelineCommand.compiApp = new CompiApp(config);

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

    RunSpecificPipelineCommand.resolver = createPipelineVariableResolver(parameters);

    compiApp.addTaskExecutionHandler(new TaskExecutionHandler() {

      private Set<String> startedForeachs = new HashSet<String>();

      @Override
      synchronized public void taskStarted(Task task) {
        if (task instanceof Foreach) {
          if (!startedForeachs.contains(task.getId())) {
            LOGGER.info(
              "> Started loop task " + task.getId()
            );
            startedForeachs.add(task.getId());
          }
        } else {
          LOGGER.info("> Started task " + task.getId());
        }
      }

      @Override
      synchronized public void taskFinished(Task task) {
        if (task instanceof Foreach) {
          LOGGER.info(
            "< Finished loop task " + task.getId()
          );
        } else {
          LOGGER.info("< Finished task " + task.getId());
        }
      }

      @Override
      synchronized public void taskAborted(Task task, CompiTaskAbortedException e) {
        LOGGER.severe(
          "X Aborted task " + task.getId() + ". Cause: " + e.getMessage() + getLogInfo(e) 
        );
      }

      @Override
      public void taskIterationStarted(ForeachIteration iteration) {
        LOGGER.info(
          ">> Started loop iteration of task " + iteration.getId()
        );
      }

      @Override
      public void taskIterationFinished(ForeachIteration iteration) {
        LOGGER.info(
          "<< Finished loop iteration of task " + iteration.getId()
        );
      }

      @Override
      public void taskIterationAborted(ForeachIteration iteration, CompiTaskAbortedException e) {
        LOGGER.severe(
          "X Aborted loop iteration of task " + iteration.getId() + ". Cause: " + e.getMessage() + getLogInfo(e)
        );

      }

      private String getLogInfo(CompiTaskAbortedException e) {        
        StringBuilder builder = new StringBuilder();
        
        if (e.getLastStdOut().size() > 0) {
          builder.append("\n");
          builder.append("--- Last "+e.getLastStdOut().size()+" stdout lines ---\n");
          e.getLastStdOut().forEach(line -> {
            builder.append(line+"\n");
          });
          builder.append("--- End of stdout lines ---\n");
        }
        
        if (e.getLastStdErr().size() > 0) {
          builder.append("\n");
          builder.append("--- Last "+e.getLastStdErr().size()+" stderr lines ---\n");
          e.getLastStdErr().forEach(line -> {
            builder.append(line+"\n");
          });
          builder.append("--- End of stderr lines ---\n");
        }
        
        if (e.getLastStdOut().size() > 0 && e.getTask().getStdOutLogFile() != null) {
          builder.append("\nComplete stdout log file: "+e.getTask().getStdOutLogFile());
        } else if (e.getLastStdOut().size() > 0) {
          builder.append("\nTask "+e.getTask().getId()+" was not recording the complete stdout in a log file. Enable logging if you want and run again. ");
        }
        if (e.getLastStdErr().size() > 0 && e.getTask().getStdErrLogFile() != null) {
          builder.append("\nComplete stderr log file: "+e.getTask().getStdErrLogFile());
        } else if (e.getLastStdErr().size() > 0) {
          builder.append("\nTask "+e.getTask().getId()+" was not recording the complete stderr in a log file. Enable logging if you want and run again. ");
        }
        return builder.toString();
      }
    });
    compiApp.run();
  }

  private static VariableResolver createPipelineVariableResolverProxy() {
    // A proxy for the variable resolver. This proxy solves a tricky situation:
    // 1. We need that CompiApp is created before the createOptions method is
    // called, since we need that skipped tasks
    // are computed (which is done inside CompiApp constructor)
    // 2. The variableResolver, that is passed to CompiApp inside configuration,
    // cannot be created until we receive the
    // call to execute(Parameters), since the Parameters object is needed to
    // create this variable resolver.

    // To solve this situation, we create CompiApp on the static
    // newRunSpecificPipelineCommand (which is called before
    // createOptions), but with a "proxy" variable resolver, that throws
    // exceptions if its methods for resolving variables
    // are called before we have the real resolver, which is constructed during
    // execute(Parameters) call. Fortunately,
    // nobody calls these variable resolving methods before execute, since we
    // have not called CompiApp.run() yet.
    return new VariableResolver() {

      @Override
      public String resolveVariable(String variable) throws IllegalArgumentException {
        if (resolver == null) {
          // you should not see this, since resolveVariable is not called before
          // compiApp.run() is called
          throw new IllegalStateException("resolver has not been initialized");
        }
        return resolver.resolveVariable(variable);
      }

      @Override
      public Set<String> getVariableNames() {
        if (resolver == null) {
          // you should not see this, since resolveVariable is not called before
          // compiApp.run() is called
          throw new IllegalStateException("resolver has not been initialized");
        }
        return resolver.getVariableNames();
      }

    };
  }

  private VariableResolver createPipelineVariableResolver(Parameters parameters) {
    Map<String, String> variables = new HashMap<>();

    // options defined as CLI parameters
    RunSpecificPipelineCommand.this.getOptions().forEach((option) -> {
      if (
          parameters
            .hasOption(
              option
        ) && (!(option instanceof DefaultValuedOption)
          ||
          !((DefaultValuedOption<?>) option)
            .getDefaultValue()
            .equals(
              parameters
                .getSingleValue(
                  option
          )
          ))
      ) {
        
        variables.put(option.getParamName(), option instanceof FlagOption? "yes": parameters.getSingleValueString(option));
      }
    });

    // options in the parameters file that have not been defined yet
    VariableResolver paramsFileResolver = getParamsFileResolver();
    if (paramsFileResolver != null) {
      paramsFileResolver.getVariableNames().forEach(variable -> {
        if (!variables.containsKey(variable)) {
          variables.put(variable, paramsFileResolver.resolveVariable(variable));
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
    List<Option<?>> options = new ArrayList<>();
    try {
      Pipeline p = config.getPipeline();

      p.getTasksByParameter().forEach((parameterName, tasks) -> {
        ParamsFileVariableResolver paramsFileResolver = getParamsFileResolver();
        ParameterDescription description = p.getParameterDescription(parameterName);
        List<OptionCategory> categories =
          tasks.stream().filter(task -> !task.isSkipped())
            .map(task -> new OptionCategory(task.getId())).collect(toList()
        );

        if (categories.size() > 0) {
          if (description != null) {

            if (categories.size() > 0) {
              Option<?> option = null;
              if (description.isFlag()) {
                option = new FlagOption(
                  categories, description.getName(), 
                  description.getShortName(), 
                  description.getDescription());
              }
              else if (description.getDefaultValue() != null) {
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
                      && paramsFileResolver.resolveVariable(parameterName
                ) != null ? true
                  : false,
                    true, false
                    );
              }
              options.add(option
          );
            }
          } else {
            options.add(
              new StringOption(
                categories, parameterName, parameterName, "",
                paramsFileResolver != null
                  && paramsFileResolver.resolveVariable(parameterName
            ) != null ? true
              : false,
                true, false
                )
                );
          }
        }

      });
    } catch (Exception e) {
      e.printStackTrace();
    }

    return options;
  }

  private ParamsFileVariableResolver getParamsFileResolver() {
    ParamsFileVariableResolver resolver = null;
    for (int i = 0; i < commandLineArgs.length; i++) {
        String arg = commandLineArgs[i];
            if (arg.equals("--params") || arg.equals("-pa")) {
                resolver = new ParamsFileVariableResolver(new File(commandLineArgs[i + 1]));
            }
    }

    return resolver;
  }
}
