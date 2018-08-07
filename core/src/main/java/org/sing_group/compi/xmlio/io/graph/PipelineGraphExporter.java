package org.sing_group.compi.xmlio.io.graph;

import static guru.nidi.graphviz.attribute.Font.config;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static org.sing_group.compi.xmlio.PipelineParserFactory.createPipelineParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.sing_group.compi.xmlio.entities.Foreach;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;

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

	private File pipeline;
	private File output;
	private OutputFormat outputFormat;
	private int width = -1;
	private int height = -1;
	private int fontSize;

	public PipelineGraphExporter(File pipeline, File output,
		OutputFormat outputFormat, int fontSize) {
		this.pipeline = pipeline;
		this.output = output;
		this.outputFormat = outputFormat;
		this.fontSize = fontSize;
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

		Graph pipelineGraph = graph("compi").directed();
		pipelineGraph = pipelineGraph.nodeAttr()
			.with(config("Arial", this.fontSize));

		Map<String, Node> idToNode = new HashMap<>();

		for (Task task : pipelineObject.getTasks()) {
			Node node = node(task.getId());
			if (Foreach.class.isAssignableFrom(task.getClass())) {
				node = node.with(Style.DASHED, Shape.RECTANGLE);
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
}
