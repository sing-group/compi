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
import static guru.nidi.graphviz.attribute.Font.config;
import static guru.nidi.graphviz.attribute.Label.html;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.xmlio.PipelineParserFactory.createPipelineParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.pipeline.Task;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

public class PipelineGraphExporter {
  public enum OutputFormat {
    PNG(Format.PNG), SVG(Format.SVG), XDOT(Format.XDOT), JSON(Format.JSON);

    private Format format;

    OutputFormat(Format format) {
      this.format = format;
    }

    public Format getFormat() {
      return format;
    }
  }

  public enum GraphOrientation {
    HORIZONTAL(RankDir.LEFT_TO_RIGHT), VERTICAL(RankDir.TOP_TO_BOTTOM);

    private RankDir rankDir;

    GraphOrientation(RankDir rankDir) {
      this.rankDir = rankDir;
    }

    public RankDir getRankDir() {
      return rankDir;
    }
  }

  public enum DrawParams {
    NO, TASK, PIPELINE;
  }

  public enum NodeStyle {
    DASHED(Style.DASHED),
    SOLID(Style.SOLID),
    INVIS(Style.INVIS),
    BOLD(Style.BOLD),
    FILLED(Style.FILLED),
    RADIAL(Style.RADIAL),
    DIAGONALS(Style.DIAGONALS),
    ROUNDED(Style.ROUNDED);

    private Style style;

    NodeStyle(Style style) {
      this.style = style;
    }

    public Style getStyle() {
      return style;
    }
  }

  private File pipeline;
  private File output;
  private OutputFormat outputFormat;
  private GraphOrientation graphOrientation;
  private int width = -1;
  private int height = -1;
  private int fontSize;
  private DrawParams drawParams;
  private int lineWidth;
  private Map<String, String> taskToRgbColor;
  private Map<String, String> taskToStyle;
  private List<String> tasksToIncludeParams = emptyList();
  private List<String> tasksToExcludeParams = emptyList();

  public PipelineGraphExporter(
    File pipeline, File output, OutputFormat outputFormat, int fontSize, GraphOrientation graphOrientation,
    DrawParams drawParams, int lineWidth, Map<String, String> taskToRgbColor, Map<String, String> taskToStyle
  ) {
    this.pipeline = pipeline;
    this.output = output;
    this.outputFormat = outputFormat;
    this.fontSize = fontSize;
    this.graphOrientation = graphOrientation;
    this.drawParams = drawParams;
    this.lineWidth = lineWidth;
    this.taskToRgbColor = taskToRgbColor;
    this.taskToStyle = taskToStyle;
  }

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

  public void export() throws IOException {
    Pipeline pipelineObject = createPipelineParser().parsePipeline(this.pipeline);

    Graphviz.useEngine(new GraphvizJdkEngine());

    Graph pipelineGraph = graph("compi").directed().graphAttr().with(this.graphOrientation.getRankDir());

    pipelineGraph = pipelineGraph.nodeAttr().with(config("Arial", this.fontSize));

    Map<String, Node> idToNode = new HashMap<>();

    for (Task task : pipelineObject.getTasks()) {
      Node node = node(task.getId());

      if (Foreach.class.isAssignableFrom(task.getClass())) {
        node = node.with(Shape.RECTANGLE, getTaskStyle(task.getId(), true));
      } else {
        node = node.with(Shape.RECTANGLE, getTaskStyle(task.getId(), false));
      }
      if (taskToRgbColor.containsKey(task.getId())) {
        node = node.with(Color.rgb(taskToRgbColor.get(task.getId())));
      }
      idToNode.putIfAbsent(task.getId(), node);
    }

		for (Task task : pipelineObject.getTasks()) {
			Node node = idToNode.get(task.getId());
			for (final String afterId : task.getAfterList()) {
        idToNode.put(afterId, idToNode.get(afterId).link(node));
      }

      if (isDrawParameters(task) && !task.getParameters().isEmpty()) {
        if (this.drawParams.equals(DrawParams.TASK)) {
          String paramsNodeName = task.getId() + "_params";
          Node params =
            node(paramsNodeName).with(
              html(task.getParameters().stream().collect(joining("<br/>")))
            ).with(getParamsNodeStyle());
          idToNode.put(paramsNodeName, params.link(to(node).with(Style.DOTTED)));
        } else {
          for (String p : task.getParameters()) {
            String paramsNodeName = p;
            Node paramsNode = null;
            if (idToNode.containsKey(paramsNodeName)) {
              paramsNode = idToNode.get(paramsNodeName);
            } else {
              paramsNode = node(paramsNodeName).with(Label.of(p)).with(getParamsNodeStyle());
            }
            idToNode.put(paramsNodeName, paramsNode.link(to(node).with(Style.DOTTED)));
          }
        }
      }
    }

		for (Node n : idToNode.values()) {
			pipelineGraph = pipelineGraph.with(n);
		}

		Graphviz graphviz = fromGraph(pipelineGraph);

		if (this.height > 0) {
			graphviz = graphviz.height(this.height);
		}

		if (this.width > 0) {
			graphviz = graphviz.width(this.width);
		}

		graphviz.render(this.outputFormat.getFormat()).toFile(this.output);
  }

  private boolean isDrawParameters(Task task) {
    if (drawParams.equals(DrawParams.NO)) {
      return false;
    }else if (!this.tasksToIncludeParams.isEmpty()) {
      return this.tasksToIncludeParams.contains(task.getId());
    } else if (!this.tasksToExcludeParams.isEmpty()) {
      return !this.tasksToExcludeParams.contains(task.getId());
    } else {
      return true;
    }
  }

  private Attributes getTaskStyle(String taskId, boolean isForeach) {
    Style style = isForeach ? Style.DASHED : Style.SOLID;
    if (this.taskToStyle.containsKey(taskId)) {
      style = NodeStyle.valueOf(this.taskToStyle.get(taskId).toUpperCase()).getStyle();
    }
    return getLineWidhStyle().and(style);
  }

  private Style getLineWidhStyle() {
    return Style.lineWidth(this.lineWidth);
  }

  private Attributes[] getParamsNodeStyle() {
    return new Attributes[] {
      getLineWidhStyle().and(Style.FILLED), Color.GRAY
    };
  }

  public void setTasksToExcludeParams(List<String> tasksToExcludeParams) {
    this.tasksToExcludeParams = tasksToExcludeParams;
    this.tasksToIncludeParams = emptyList();
  }

  public void setTasksToIncludeParams(List<String> tasksToIncludeParams) {
    this.tasksToIncludeParams = tasksToIncludeParams;
    this.tasksToExcludeParams = emptyList();
  }
}
