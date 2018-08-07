package org.sing_group.compi.xmlio.io.graph;

import java.io.File;

import org.sing_group.compi.xmlio.io.graph.PipelineGraphExporter.OutputFormat;

public class PipelineGraphExporterBuilder {
	public static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.PNG;
	public static final int DEFAULT_WIDTH = -1;
	public static final int DEFAULT_HEIGHT = -1;
	public static final int DEFAULT_FONT_SIZE = 10;

	private File pipeline;
	private File output;
	private OutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	private int fontSize = DEFAULT_FONT_SIZE;

	public PipelineGraphExporterBuilder(File pipeline, File output) {
		this.pipeline = pipeline;
		this.output = output;
	}

	public PipelineGraphExporterBuilder outputFormat(String outputFormat) {
		this.outputFormat = OutputFormat.valueOf(outputFormat.toUpperCase());
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

	public PipelineGraphExporter build() {
		PipelineGraphExporter toret = new PipelineGraphExporter(
			this.pipeline, this.output, this.outputFormat, this.fontSize);

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
}
