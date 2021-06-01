/*-
 * #%L
 * Compi CLI
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sing_group.compi.cli.commands;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.CompiRunConfiguration;
import org.sing_group.compi.core.pipeline.ParameterDescription;
import org.sing_group.compi.core.pipeline.Pipeline;
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

  public static final String NAME = "run";

  private static CompiRunConfiguration config;
  private static CompiApp compiApp;
  private static ResolverProxy proxy = null;
  private static File paramsFile = null;

  public static RunSpecificPipelineCommand newRunSpecificPipelineCommand(
    CompiRunConfiguration config
  ) throws IOException {

    proxy = new ResolverProxy();
    config.setResolver(proxy);

    RunSpecificPipelineCommand.config = config;
    RunSpecificPipelineCommand.paramsFile = config.getParamsFile();
    /**
     * It is necessary to store the params file in a variable and remove it from
     * the configuration in order to avoid that the CompiApp class uses it by
     * creating a ParamsFileVariableResolver. By removing it, we will force the
     * CompiApp to use the proxy variable resolver created here.
     */
    config.setParamsFile(null);

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

    proxy.setResolver(createPipelineVariableResolver(parameters));

    compiApp.addTaskExecutionHandler(new CLITaskExecutionHandler());
    compiApp.run();
  }

  private static class ResolverProxy implements VariableResolver {
    private static final long serialVersionUID = 1L;
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

    private VariableResolver resolver;

    @Override
    public String resolveVariable(String variable) throws IllegalArgumentException {
      if (this.resolver == null) {
        // you should not see this, since resolveVariable is not called before
        // compiApp.run() is called
        throw new IllegalStateException("resolver has not been initialized");
      }
      return this.resolver.resolveVariable(variable);
    }

    @Override
    public Set<String> getVariableNames() {
      if (this.resolver == null) {
        // you should not see this, since resolveVariable is not called before
        // compiApp.run() is called
        throw new IllegalStateException("resolver has not been initialized");
      }
      return this.resolver.getVariableNames();
    }

    public void setResolver(VariableResolver resolver) {
      System.out.println("setting resolver " + resolver);
      this.resolver = resolver;
    }
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

        variables
          .put(option.getParamName(), option instanceof FlagOption ? "yes" : parameters.getSingleValueString(option));
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
                option =
                  new FlagOption(
                    categories, description.getName(),
                    description.getShortName(),
                    description.getDescription()
                  );
              } else if (description.getDefaultValue() != null) {
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
                      && paramsFileResolver.resolveVariable(
                        parameterName
                      ) != null ? true
                        : false,
                    true, false
                  );
              }
              options.add(
                option
              );
            }
          } else {
            options.add(
              new StringOption(
                categories, parameterName, parameterName, "",
                paramsFileResolver != null
                  && paramsFileResolver.resolveVariable(
                    parameterName
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
    if (paramsFile != null) {
      return new ParamsFileVariableResolver(paramsFile);
    }
    {
      return null;
    }
  }
}
