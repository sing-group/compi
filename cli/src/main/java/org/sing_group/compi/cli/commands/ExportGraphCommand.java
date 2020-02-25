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

import static java.util.Arrays.asList;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.DEFAULT_FONT_SIZE;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.DEFAULT_GRAPH_ORIENTATION;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.DEFAULT_LINE_WIDTH;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.DEFAULT_OUTPUT_FORMAT;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.isValidGraphOrientation;
import static org.sing_group.compi.io.graph.PipelineGraphExporterBuilder.isValidOutputFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.sing_group.compi.io.graph.PipelineGraphExporter.DrawParams;
import org.sing_group.compi.io.graph.PipelineGraphExporter.GraphOrientation;
import org.sing_group.compi.io.graph.PipelineGraphExporter.NodeStyle;
import org.sing_group.compi.io.graph.PipelineGraphExporter.OutputFormat;
import org.sing_group.compi.core.validation.PipelineValidator;
import org.sing_group.compi.core.validation.ValidationError;
import org.sing_group.compi.io.graph.PipelineGraphExporterBuilder;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.FlagOption;
import es.uvigo.ei.sing.yacli.command.option.IntegerDefaultValuedStringConstructedOption;
import es.uvigo.ei.sing.yacli.command.option.IntegerOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class ExportGraphCommand extends AbstractCommand {
  private static final Logger LOGGER = getLogger(ExportGraphCommand.class.getName());

  private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
  private static final String OUTPUT_FILE = "o";
  private static final String GRAPH_FORMAT = "f";
  private static final String GRAPH_ORIENTATION = "or";
  private static final String GRAPH_WIDTH = "w";
  private static final String GRAPH_HEIGHT = "h";
  private static final String GRAPH_FONT_SIZE = "fs";
  private static final String DRAW_PIPELINE_PARAMS = "dpp";
  private static final String DRAW_TASK_PARAMS = "dtp";
  private static final String LINE_WIDTH = "lw";
  private static final String TASK_STYLES = "te";
  private static final String TASK_COLORS = "tc";
  private static final String PARAMS_INCLUDE_TASKS = "it";
  private static final String PARAMS_EXCLUDE_TASKS = "et";

  private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;
  private static final String OUTPUT_FILE_LONG = "output";
  private static final String GRAPH_FORMAT_LONG = "format";
  private static final String GRAPH_ORIENTATION_LONG = "orientation";
  private static final String GRAPH_WITH_LONG = "width";
  private static final String GRAPH_HEIGHT_LONG = "height";
  private static final String GRAPH_FONT_SIZE_LONG = "font-size";
  private static final String DRAW_PIPELINE_PARAMS_LONG = "draw-pipeline-params";
  private static final String DRAW_TASK_PARAMS_LONG = "draw-task-params";
  private static final String LINE_WIDTH_LONG = "line-width";
  private static final String TASK_STYLES_LONG = "task-styles";
  private static final String TASK_COLORS_LONG = "task-colors";
  private static final String PARAMS_INCLUDE_TASKS_LONG = "include-task-params";
  private static final String PARAMS_EXCLUDE_TASKS_LONG = "exclude-task-params";

  private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;
  private static final String OUTPUT_FILE_DESCRIPTION = "output file";
  private static final String GRAPH_FORMAT_DESCRIPTION =
    "graph format. Values: " + getOutputformatValues();
  private static final String GRAPH_ORIENTATION_DESCRIPTION =
    "graph orientation. Values: " + getGraphOrientationValues();
  private static final String GRAPH_WIDTH_DESCRIPTION =
    "graph width. By default, no width is used so the graph takes "
      + "the minimum required. This option is incompatible with --" + GRAPH_HEIGHT_LONG;
  private static final String GRAPH_HEIGHT_DESCRIPTION =
    "graph height. By default, no height is used so the graph "
      + "takes the minimum required. This option is incompatible with --" + GRAPH_WITH_LONG;
  private static final String GRAPH_FONT_SIZE_DESCRIPTION = "graph font size";
  private static final String DRAW_PIPELINE_PARAMS_DESCRIPTION =
    "use this flag to draw one node for each pipeline "
      + "parameter. Each parameter node will be connected to the tasks using them. This flag is incompatible with --"
      + DRAW_TASK_PARAMS_LONG;
  private static final String DRAW_TASK_PARAMS_DESCRIPTION =
    "use this flag to draw one node for each task with all the"
      + " task parameters. This flag is incompatible with --" + DRAW_PIPELINE_PARAMS_LONG;
  private static final String LINE_WIDTH_DESCRIPTION = "the line width of the graph nodes";
  private static final String TASK_STYLES_DESCRIPTION =
    "the styles to the draw the task nodes. Use the following "
      + "format: task-id-1:style;task-id-2,task-id-3:style. Possible values for styles: " + getTaskStylesValues();
  private static final String TASK_COLORS_DESCRIPTION =
    "the colors to the draw the task nodes. Colors must be "
      + "specified using their corresponding hexadecimal codes. Use the following format: "
      + "task-id-1:color;task-id-2,task-id-3:color";
  private static final String PARAMS_INCLUDE_TASKS_DESCRIPTION =
    "when draw parameters options ("
      + DRAW_PIPELINE_PARAMS_LONG + " or " + DRAW_TASK_PARAMS_LONG + ") are used, this option specifies the tasks for "
      + "which parameter nodes should be created or parameters should be linked to. Task identifiers must be separated "
      + "by commas. This option is incompatible with --" + PARAMS_EXCLUDE_TASKS_LONG;
  private static final String PARAMS_EXCLUDE_TASKS_DESCRIPTION =
    "when draw parameters options ("
      + DRAW_PIPELINE_PARAMS_LONG + " or " + DRAW_TASK_PARAMS_LONG + ") are used, this option specifies the tasks for "
      + "which parameter nodes should not be created or parameters should not be linked to. Task identifiers must be "
      + "separated by commas. This option is incompatible with --" + PARAMS_INCLUDE_TASKS_LONG;

  private static final String DEFAULT_OUTPUT_FORMAT_STRING = DEFAULT_OUTPUT_FORMAT.toString().toLowerCase();

  private static final String DEFAULT_GRAPH_ORIENTATION_STRING = DEFAULT_GRAPH_ORIENTATION.toString().toLowerCase();

  @Override
  public void execute(final Parameters parameters) throws Exception {
    File pipelineFile = new File(parameters.getSingleValueString(super.getOption(PIPELINE_FILE)));

    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException("Pipeline file not found: " + pipelineFile);
    }

    LOGGER.info("Pipeline file - " + pipelineFile);

    List<ValidationError> errors = new PipelineValidator(pipelineFile).validate();
    ValidatePipelineCommand.logValidationErrors(errors, LOGGER);

    if (
      errors.stream().filter(error -> error.getType().isError())
        .count() > 0
    ) {
      System.exit(1);
    }

    File outputFile = new File(parameters.getSingleValueString(super.getOption(OUTPUT_FILE)));

    LOGGER.info("Export graph to file - " + outputFile);

    if (outputFile.getParentFile() != null && !outputFile.getParentFile().canWrite()) {
      throw new IllegalArgumentException("Can't write output file: " + outputFile);
    }

    PipelineGraphExporterBuilder graphExporterBuilder = new PipelineGraphExporterBuilder(pipelineFile, outputFile);

    String graphFormat = parameters.getSingleValueString(super.getOption(GRAPH_FORMAT));

    if (isValidOutputFormat(graphFormat)) {
      LOGGER.info("Graph format - " + graphFormat);
    } else {
      throw new IllegalArgumentException(
        "The specified graph format ("
          + graphFormat + ") is not valid. Valid values: "
          + getOutputformatValues()
      );
    }

    if (!graphFormat.equals(DEFAULT_OUTPUT_FORMAT_STRING)) {
      graphExporterBuilder = graphExporterBuilder.outputFormat(graphFormat);
    }

    String graphOrientation = parameters.getSingleValueString(super.getOption(GRAPH_ORIENTATION));

    if (isValidGraphOrientation(graphOrientation)) {
      LOGGER.info("Graph orientation - " + graphOrientation);
    } else {
      throw new IllegalArgumentException(
        "The specified graph orientation (" + graphOrientation
          + ") is not valid. Valid values: "
          + getGraphOrientationValues()
      );
    }

    if (!graphOrientation.equals(DEFAULT_GRAPH_ORIENTATION_STRING)) {
      graphExporterBuilder = graphExporterBuilder.graphOrientation(graphOrientation);
    }

    boolean hasWidth = parameters.hasOption(super.getOption(GRAPH_WIDTH));
    boolean hasHeight = parameters.hasOption(super.getOption(GRAPH_HEIGHT));

    if (hasWidth && hasHeight) {
      throw new IllegalArgumentException(
        "You can specify " + GRAPH_WITH_LONG + " or "
          + GRAPH_HEIGHT_LONG + ", but not both at the same time."
      );
    }

    if (hasWidth) {
      Integer graphWidth = parameters.getSingleValue(super.getOption(GRAPH_WIDTH));
      LOGGER.info("Graph width - " + graphWidth);
      graphExporterBuilder = graphExporterBuilder.width(graphWidth);
    } else if (hasHeight) {
      Integer graphHeight = parameters.getSingleValue(super.getOption(GRAPH_HEIGHT));
      LOGGER.info("Graph height - " + graphHeight);
      graphExporterBuilder = graphExporterBuilder.height(graphHeight);
    }

    Integer fontSize = parameters.getSingleValue(super.getOption(GRAPH_FONT_SIZE));
    LOGGER.info("Graph font size - " + fontSize);

    if (fontSize != DEFAULT_FONT_SIZE) {
      graphExporterBuilder = graphExporterBuilder.fontSize(fontSize);
    }

    boolean hasDrawPipelineParams = parameters.hasFlag(super.getOption(DRAW_PIPELINE_PARAMS));
    boolean hasDrawTaskParams = parameters.hasFlag(super.getOption(DRAW_TASK_PARAMS));

    if (hasDrawPipelineParams && hasDrawTaskParams) {
      throw new IllegalArgumentException(
        "You can specify " + DRAW_PIPELINE_PARAMS_LONG + " or "
          + DRAW_TASK_PARAMS_LONG + ", but not both at the same time."
      );
    } else {
      DrawParams drawParams = DrawParams.NO;
      if (hasDrawPipelineParams) {
        drawParams = DrawParams.PIPELINE;
        LOGGER.info("Draw pipeline parameters");
      }
      if (hasDrawTaskParams) {
        drawParams = DrawParams.TASK;
        LOGGER.info("Draw task parameters");
      }
      graphExporterBuilder = graphExporterBuilder.drawParams(drawParams);
    }

    boolean hasIncludeParamsTasks = parameters.hasOption(super.getOption(PARAMS_INCLUDE_TASKS));
    boolean hasExcludeParamsTasks = parameters.hasOption(super.getOption(PARAMS_EXCLUDE_TASKS));

    if (hasIncludeParamsTasks && hasExcludeParamsTasks) {
      throw new IllegalArgumentException(
        "You can specify " + PARAMS_INCLUDE_TASKS_LONG + " or "
          + PARAMS_EXCLUDE_TASKS_LONG + ", but not both at the same time."
      );
    } else if (hasDrawPipelineParams || hasDrawTaskParams) {
      if (hasIncludeParamsTasks) {
        List<String> tasksToIncludeParams =
          parseTasksList(parameters.getSingleValue(super.getOption(PARAMS_INCLUDE_TASKS)));
        graphExporterBuilder = graphExporterBuilder.tasksToIncludeParams(tasksToIncludeParams);
        LOGGER.info("Draw parameters only for these tasks: " + tasksToIncludeParams.stream().collect(joining(", ")));
      } else if (hasExcludeParamsTasks) {
        List<String> tasksToExcludeParams =
          parseTasksList(parameters.getSingleValue(super.getOption(PARAMS_EXCLUDE_TASKS)));
        graphExporterBuilder = graphExporterBuilder.tasksToExcludeParams(tasksToExcludeParams);
        LOGGER
          .info("Tasks for which parameters are not shown: " + tasksToExcludeParams.stream().collect(joining(", ")));
      } else {
        if (hasIncludeParamsTasks) {
          LOGGER.warning(
            "Ignoring --" + PARAMS_INCLUDE_TASKS_LONG + " because " + DRAW_PIPELINE_PARAMS_LONG + " or "
              + DRAW_TASK_PARAMS_LONG + " are not present"
          );
        }
      }
    }

    Integer lineWidth = parameters.getSingleValue(super.getOption(LINE_WIDTH));
    LOGGER.info("Line width - " + lineWidth);

    if (lineWidth != DEFAULT_LINE_WIDTH) {
      graphExporterBuilder = graphExporterBuilder.lineWidth(lineWidth);
    }

    if (parameters.hasOption(super.getOption(TASK_COLORS))) {
      String taskColors = parameters.getSingleValueString(super.getOption(TASK_COLORS));
      graphExporterBuilder = graphExporterBuilder.taskColors(parseTaskColors(taskColors));
    }

    if (parameters.hasOption(super.getOption(TASK_STYLES))) {
      String taskStyles = parameters.getSingleValueString(super.getOption(TASK_STYLES));
      graphExporterBuilder = graphExporterBuilder.taskStyles(parseTaskStyles(taskStyles));
    }

    graphExporterBuilder.build().export();
  }

  @Override
  public String getDescription() {
    return "Exports a pipeline to a graph file.";
  }

  @Override
  public String getName() {
    return "export-graph";
  }

  @Override
  public String getDescriptiveName() {
    return "Export to graph file";
  }

  @Override
  protected List<Option<?>> createOptions() {
    final List<Option<?>> options = new ArrayList<>();
    options.add(getPipelineFileOption());
    options.add(getOutputFileOption());

    options.add(getOutputFormatOption());
    options.add(getGraphOrientationOption());
    options.add(getWidthOption());
    options.add(getHeightOption());
    options.add(getFontSizeOption());
    options.add(getLineWidthOption());
    options.add(getTaskColorsOption());
    options.add(getNodeStylesOption());
    options.add(getDrawPipelineParamsOption());
    options.add(getDrawTaskParamsOption());
    options.add(getParamsIncludeTasksOption());
    options.add(getParamsExcludeTasksOption());

    return options;
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE,
      PIPELINE_FILE_DESCRIPTION, CommonParameters.PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  private Option<?> getOutputFileOption() {
    return new StringOption(
      OUTPUT_FILE_LONG, OUTPUT_FILE,
      OUTPUT_FILE_DESCRIPTION, false, true, false
    );
  }

  private Option<?> getOutputFormatOption() {
    return new DefaultValuedStringOption(
      GRAPH_FORMAT_LONG, GRAPH_FORMAT,
      GRAPH_FORMAT_DESCRIPTION, DEFAULT_OUTPUT_FORMAT_STRING
    );
  }

  private Option<?> getGraphOrientationOption() {
    return new DefaultValuedStringOption(
      GRAPH_ORIENTATION_LONG, GRAPH_ORIENTATION,
      GRAPH_ORIENTATION_DESCRIPTION, DEFAULT_GRAPH_ORIENTATION_STRING
    );
  }

  private Option<?> getWidthOption() {
    return new IntegerOption(
      GRAPH_WITH_LONG, GRAPH_WIDTH,
      GRAPH_WIDTH_DESCRIPTION, true
    );
  }

  private Option<?> getHeightOption() {
    return new IntegerOption(
      GRAPH_HEIGHT_LONG, GRAPH_HEIGHT,
      GRAPH_HEIGHT_DESCRIPTION, true
    );
  }

  private Option<?> getFontSizeOption() {
    return new IntegerDefaultValuedStringConstructedOption(
      GRAPH_FONT_SIZE_LONG, GRAPH_FONT_SIZE,
      GRAPH_FONT_SIZE_DESCRIPTION, DEFAULT_FONT_SIZE
    );
  }

  private Option<?> getDrawPipelineParamsOption() {
    return new FlagOption(
      DRAW_PIPELINE_PARAMS_LONG, DRAW_PIPELINE_PARAMS, DRAW_PIPELINE_PARAMS_DESCRIPTION
    );
  }

  private Option<?> getDrawTaskParamsOption() {
    return new FlagOption(
      DRAW_TASK_PARAMS_LONG, DRAW_TASK_PARAMS, DRAW_TASK_PARAMS_DESCRIPTION
    );
  }

  private Option<?> getLineWidthOption() {
    return new IntegerDefaultValuedStringConstructedOption(
      LINE_WIDTH_LONG, LINE_WIDTH,
      LINE_WIDTH_DESCRIPTION, DEFAULT_LINE_WIDTH
    );
  }

  private Option<?> getParamsIncludeTasksOption() {
    return new StringOption(
      PARAMS_INCLUDE_TASKS_LONG, PARAMS_INCLUDE_TASKS,
      PARAMS_INCLUDE_TASKS_DESCRIPTION, true, true, false
    );
  }

  private Option<?> getParamsExcludeTasksOption() {
    return new StringOption(
      PARAMS_EXCLUDE_TASKS_LONG, PARAMS_EXCLUDE_TASKS,
      PARAMS_EXCLUDE_TASKS_DESCRIPTION, true, true, false
    );
  }

  private Option<?> getNodeStylesOption() {
    return new StringOption(
      TASK_STYLES_LONG, TASK_STYLES,
      TASK_STYLES_DESCRIPTION, true, true, false
    );
  }

  private Option<?> getTaskColorsOption() {
    return new StringOption(
      TASK_COLORS_LONG, TASK_COLORS,
      TASK_COLORS_DESCRIPTION, true, true, false
    );
  }

  private static final String getOutputformatValues() {
    return asList(OutputFormat.values()).stream()
      .map(OutputFormat::toString).map(String::toLowerCase)
      .collect(joining(", "));
  }

  private static final String getGraphOrientationValues() {
    return asList(GraphOrientation.values()).stream()
      .map(GraphOrientation::toString).map(String::toLowerCase)
      .collect(joining(", "));
  }

  private static final String getTaskStylesValues() {
    return asList(NodeStyle.values()).stream()
      .map(NodeStyle::toString).map(String::toLowerCase)
      .collect(joining(", "));
  }

  private Map<String, String> parseTaskColors(String taskColors) {
    return parseTaskAttributes(
      taskColors, "Ignoring color definition string \" %s \" since it does not follow the required format."
    );
  }

  private Map<String, String> parseTaskStyles(String taskStyles) {
    return parseTaskAttributes(
      taskStyles, "Ignoring style definition string \" %s \" since it does not follow the required format."
    );
  }

  private Map<String, String> parseTaskAttributes(String taskAttributes, String ignoreDefinitionString) {
    Map<String, String> attributesMap = new HashMap<>();
    String[] attributesArray = taskAttributes.split(";");
    for (String attributeDefinition : attributesArray) {
      String[] attributeDefinitionSplit = attributeDefinition.split(":");
      if (attributeDefinitionSplit.length != 2) {
        LOGGER.warning(String.format(ignoreDefinitionString, attributeDefinition));
      } else {
        String tasksList = attributeDefinitionSplit[0];
        String attribute = attributeDefinitionSplit[1];
        for (String task : tasksList.split(",")) {
          attributesMap.put(task, attribute);
        }
      }
    }

    return attributesMap;
  }

  private List<String> parseTasksList(String tasksList) {
    return Arrays.asList(tasksList.split(","));
  }
}
