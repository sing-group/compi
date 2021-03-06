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
package org.sing_group.compi.cli;

import static es.uvigo.ei.sing.yacli.command.CommandPrinter.printCommandOptionsExtended;
import static java.util.Arrays.asList;
import static org.sing_group.compi.cli.commands.RunSpecificPipelineCommand.newRunSpecificPipelineCommand;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.sing_group.compi.cli.commands.RunCommand;
import org.sing_group.compi.cli.commands.RunSpecificPipelineCommand;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.CompiRunConfiguration;

import es.uvigo.ei.sing.yacli.CLIApplication;
import es.uvigo.ei.sing.yacli.CLIApplicationCommandException;
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.Command;
import es.uvigo.ei.sing.yacli.command.CommandPrinterConfiguration;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class PipelineCLIApplication extends CLIApplication {

  private static CompiRunConfiguration config;
  private static String[] commandLineArgs;

  private PrintStream out;
  private String pipelineName;

  public static PipelineCLIApplication newPipelineCLIApplication(
    String pipelineName,
    CompiRunConfiguration config,
    String[] commandLineArgs,
    PrintStream out
  ) {

    PipelineCLIApplication.config = config;
    PipelineCLIApplication.commandLineArgs = commandLineArgs;

    return new PipelineCLIApplication(pipelineName, out);
  }

  private PipelineCLIApplication(String pipelineName, PrintStream out) {
    this.pipelineName = pipelineName;
    this.out = out;
  }

  @Override
  protected String getApplicationName() {
    return "Compi - pipeline: " + this.pipelineName;
  }

  @Override
  protected String getApplicationVersion() {
    return CompiApp.getCompiVersion();
  }

  @Override
  protected String getApplicationCommand() {
    return "compi";
  }

  @Override
  protected List<Command> buildCommands() {
    try {
      RunSpecificPipelineCommand runPipelineCommand = newRunSpecificPipelineCommand(config);
      return asList(
        runPipelineCommand,
        new ShowPipelineHelp(runPipelineCommand),
        new ShowTaskHelp(runPipelineCommand)
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  protected void printTaskHelp(Command command, PrintStream out) {
    printCommandOptionsExtended(command, out, new CommandPrinterConfiguration(false));
  }

  @Override
  protected void handleCommandException(CLIApplicationCommandException exception, PrintStream out) {
    super.handleCommandException(exception, this.out);
  }

  @Override
  public void run(String[] args) {
    super.run(args);
  }

  private class ShowPipelineHelp extends AbstractCommand {

    private RunSpecificPipelineCommand runPipelineCommand;

    public ShowPipelineHelp(RunSpecificPipelineCommand runPipelineCommand) {
      this.runPipelineCommand = runPipelineCommand;
    }

    @Override
    public String getName() {
      return "help-pipeline";
    }

    @Override
    public String getDescriptiveName() {
      return "shows help of the pipeline";
    }

    @Override
    public String getDescription() {
      return "shows help of the pipeline";
    }

    @Override
    public void execute(Parameters parameters) throws Exception {
      printCommandHelp(this.runPipelineCommand, System.err);
    }

    @Override
    protected List<Option<?>> createOptions() {
      return new ArrayList<>();
    }
  }

  private class ShowTaskHelp extends AbstractCommand {

    private RunSpecificPipelineCommand runPipelineCommand;

    public ShowTaskHelp(RunSpecificPipelineCommand runPipelineCommand) {
      this.runPipelineCommand = runPipelineCommand;
    }

    @Override
    public String getName() {
      return "help-task";
    }

    @Override
    public String getDescriptiveName() {
      return "shows help of a task";
    }

    @Override
    public String getDescription() {
      return "shows help of a task";
    }

    @Override
    public void execute(Parameters parameters) throws Exception {
      printTaskHelp(this.runPipelineCommand, System.err);
    }

    @Override
    protected List<Option<?>> createOptions() {
      return new ArrayList<>();
    }

  }
}
