package org.sing_group.compi.io.graph;

import java.io.File;

import org.sing_group.compi.io.graph.PipelineGraphExporter.DrawParams;
import org.sing_group.compi.io.graph.PipelineGraphExporter.GraphOrientation;
import org.sing_group.compi.io.graph.PipelineGraphExporter.OutputFormat;

public class PipelineGraphExporterBuilder {
  public static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
  public static final GraphOrientation DEFAULT_GRAPH_ORIENTATION = GraphOrientation.VERTICAL;
  public static final int DEFAULT_FONT_SIZE = 10;
  public static final DrawParams DEFAULT_DRAW_PARAMS = DrawParams.NO;

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

  public PipelineGraphExporter build() {
    PipelineGraphExporter toret =
      new PipelineGraphExporter(
        this.pipeline,
        this.output, this.outputFormat, this.fontSize,
        this.graphOrientation, this.drawParams
      );

    if (this.width > 0) {
      toret.setWidth(this.width);
    }

    if (this.height > 0) {
      toret.setHeight(this.height);
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
