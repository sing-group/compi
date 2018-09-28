package org.sing_group.compi.io.graph;
import static guru.nidi.graphviz.attribute.Font.config;
import static guru.nidi.graphviz.attribute.Label.html;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.xmlio.PipelineParserFactory.createPipelineParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

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

	private File pipeline;
	private File output;
	private OutputFormat outputFormat;
	private GraphOrientation graphOrientation;
	private int width = -1;
	private int height = -1;
	private int fontSize;
	private DrawParams drawParams;

  public PipelineGraphExporter(
    File pipeline, File output,
    OutputFormat outputFormat, int fontSize,
    GraphOrientation graphOrientation, DrawParams drawParams
  ) {
    this.pipeline = pipeline;
    this.output = output;
    this.outputFormat = outputFormat;
    this.fontSize = fontSize;
    this.graphOrientation = graphOrientation;
    this.drawParams = drawParams;
  }

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void export() throws IOException {
		Pipeline pipelineObject =
			createPipelineParser().parsePipeline(this.pipeline);

		Graphviz.useEngine(new GraphvizJdkEngine());

		Graph pipelineGraph = graph("compi").directed()
			.graphAttr().with(this.graphOrientation.getRankDir());

		pipelineGraph = pipelineGraph.nodeAttr()
			.with(config("Arial", this.fontSize));

		Map<String, Node> idToNode = new HashMap<>();

		for (Task task : pipelineObject.getTasks()) {
			Node node = node(task.getId());
			if (Foreach.class.isAssignableFrom(task.getClass())) {
				node = node.with(Style.DASHED, Shape.RECTANGLE);
			} else {
				node = node.with(Shape.RECTANGLE);
			}
			idToNode.putIfAbsent(task.getId(), node);
		}

		for (Task task : pipelineObject.getTasks()) {
			Node node = idToNode.get(task.getId());
			if (task.getAfter() != null) {
				for (final String afterId : task.getAfter().split(",")) {
					idToNode.put(afterId, idToNode.get(afterId).link(node));
				}
			}

      if (isDrawParameters() && !task.getParameters().isEmpty()) {
        if (this.drawParams.equals(DrawParams.TASK)) {
          String paramsNodeName = task.getId() + "_params";
          Node params =
            node(paramsNodeName).with(
              html(task.getParameters().stream().collect(joining("<br/>"))), 
              Style.FILLED, Color.GRAY
            );
          idToNode.put(paramsNodeName, params.link(to(node).with(Style.DOTTED)));
        } else {
          for (String p : task.getParameters()) {
            String paramsNodeName = p;
            Node paramsNode = null;
            if (idToNode.containsKey(paramsNodeName)) {
              paramsNode = idToNode.get(paramsNodeName);
            } else {
              paramsNode = node(paramsNodeName).with(Label.of(p), Style.FILLED, Color.GRAY);
            }
            idToNode.remove(paramsNodeName);
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

  private boolean isDrawParameters() {
    return !drawParams.equals(DrawParams.NO);
  }
}
