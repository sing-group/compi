/*-
 * #%L
 * Compi Core
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
package org.sing_group.compi.io.graph;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.sing_group.compi.io.graph.PipelineGraphExporter.DrawParams;
import org.sing_group.compi.io.graph.PipelineGraphExporter.GraphOrientation;
import org.sing_group.compi.io.graph.PipelineGraphExporter.OutputFormat;

public class PipelineGraphExporterBuilder {
  public static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
  public static final GraphOrientation DEFAULT_GRAPH_ORIENTATION = GraphOrientation.VERTICAL;
  public static final int DEFAULT_FONT_SIZE = 10;
  public static final DrawParams DEFAULT_DRAW_PARAMS = DrawParams.NO;
  public static final int DEFAULT_LINE_WIDTH = 1;

  private static final int DEFAULT_WIDTH = -1;
  private static final int DEFAULT_HEIGHT = -1;

  private File pipeline;
  private File output;
  private OutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;
  private GraphOrientation graphOrientation = DEFAULT_GRAPH_ORIENTATION;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private int fontSize = DEFAULT_FONT_SIZE;
  private DrawParams drawParams = DEFAULT_DRAW_PARAMS;
  private int lineWidth = DEFAULT_LINE_WIDTH;
  private Map<String, String> taskToRgbColor = emptyMap();
  private Map<String, String> taskToStyle = emptyMap();
  private List<String> tasksToIncludeParams = emptyList();
  private List<String> tasksToExcludeParams = emptyList();

  public PipelineGraphExporterBuilder(File pipeline, File output) {
    this.pipeline = pipeline;
    this.output = output;
  }

  public PipelineGraphExporterBuilder outputFormat(String outputFormat) {
    this.outputFormat = OutputFormat.valueOf(outputFormat.toUpperCase());
    return this;
  }

  public PipelineGraphExporterBuilder graphOrientation(String graphOrientation) {
    this.graphOrientation = GraphOrientation.valueOf(graphOrientation.toUpperCase());
    return this;
  }

  public PipelineGraphExporterBuilder width(int width) {
    this.width = width;
    return this;
  }

  public PipelineGraphExporterBuilder height(int height) {
    this.height = height;
    return this;
  }

  public PipelineGraphExporterBuilder fontSize(int fontSize) {
    this.fontSize = fontSize;
    return this;
  }

  public PipelineGraphExporterBuilder drawParams(DrawParams drawParams) {
    this.drawParams = drawParams;
    return this;
  }

  public PipelineGraphExporterBuilder lineWidth(int lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  public PipelineGraphExporterBuilder taskColors(Map<String, String> taskToRgbColor) {
    this.taskToRgbColor = taskToRgbColor;
    return this;
  }

  public PipelineGraphExporterBuilder taskStyles(Map<String, String> taskToStyle) {
    this.taskToStyle = taskToStyle;
    return this;
  }

  public PipelineGraphExporterBuilder tasksToIncludeParams(List<String> taskToIncludeParams) {
    this.tasksToIncludeParams = taskToIncludeParams;
    this.tasksToExcludeParams = emptyList();
    return this;
  }

  public PipelineGraphExporterBuilder tasksToExcludeParams(List<String> taskToExcludeParams) {
    this.tasksToExcludeParams = taskToExcludeParams;
    this.tasksToIncludeParams = emptyList();
    return this;
  }

  public PipelineGraphExporter build() {
    PipelineGraphExporter toret =
      new PipelineGraphExporter(
        this.pipeline, this.output, this.outputFormat, this.fontSize, this.graphOrientation, this.drawParams,
        this.lineWidth, this.taskToRgbColor, this.taskToStyle
      );

    if (this.width > 0) {
      toret.setWidth(this.width);
    }

    if (this.height > 0) {
      toret.setHeight(this.height);
    }

    if (!this.tasksToExcludeParams.isEmpty()) {
      toret.setTasksToExcludeParams(this.tasksToExcludeParams);
    } if (!this.tasksToIncludeParams.isEmpty()) {
      toret.setTasksToIncludeParams(this.tasksToIncludeParams);
    }

    return toret;
  }

  public static boolean isValidOutputFormat(String outputFormat) {
    try {
      OutputFormat.valueOf(outputFormat.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidGraphOrientation(String graphOrientation) {
    try {
      GraphOrientation.valueOf(graphOrientation.toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
