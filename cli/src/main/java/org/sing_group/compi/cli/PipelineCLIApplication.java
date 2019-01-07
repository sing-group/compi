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
import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.Command;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class PipelineCLIApplication extends CLIApplication {

  private static CompiRunConfiguration config;
  private static String[] commandLineArgs;

  private String pipelineName;

  public static PipelineCLIApplication newPipelineCLIApplication(
    String pipelineName,
    CompiRunConfiguration config,
    String[] commandLineArgs
  ) {

    PipelineCLIApplication.config = config;
    PipelineCLIApplication.commandLineArgs = commandLineArgs;

    return new PipelineCLIApplication(pipelineName);
  }

  private PipelineCLIApplication(String pipelineName) {
    this.pipelineName = pipelineName;
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
      RunSpecificPipelineCommand runPipelineCommand = newRunSpecificPipelineCommand(config, commandLineArgs);
      return asList(
        runPipelineCommand,
        new ShowPipelineHelp(runPipelineCommand)
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
}
