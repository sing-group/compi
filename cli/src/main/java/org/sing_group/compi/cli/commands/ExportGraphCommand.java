package org.sing_group.compi.cli.commands;

import static java.util.Arrays.asList;
import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.joining;
import static org.sing_group.compi.xmlio.io.graph.PipelineGraphExporterBuilder.DEFAULT_FONT_SIZE;
import static org.sing_group.compi.xmlio.io.graph.PipelineGraphExporterBuilder.DEFAULT_HEIGHT;
import static org.sing_group.compi.xmlio.io.graph.PipelineGraphExporterBuilder.DEFAULT_OUTPUT_FORMAT;
import static org.sing_group.compi.xmlio.io.graph.PipelineGraphExporterBuilder.DEFAULT_WIDTH;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.sing_group.compi.xmlio.io.graph.PipelineGraphExporter.OutputFormat;
import org.sing_group.compi.xmlio.io.graph.PipelineGraphExporterBuilder;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.IntegerOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.option.StringOption;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class ExportGraphCommand extends AbstractCommand {
	private static final Logger LOGGER = getLogger(ExportGraphCommand.class.getName());

	private static final String PIPELINE_FILE = CommonParameters.PIPELINE_FILE;
	private static final String OUTPUT_FILE = "o";
	private static final String GRAPH_FORMAT = "f";
	private static final String GRAPH_WIDTH = "w";
	private static final String GRAPH_HEIGHT = "h";
	private static final String GRAPH_FONT_SIZE = "fs";

	private static final String PIPELINE_FILE_LONG = CommonParameters.PIPELINE_FILE_LONG;;
	private static final String OUTPUT_FILE_LONG = "output";
	private static final String GRAPH_FORMAT_LONG = "format";
	private static final String GRAPH_WITH_LONG = "width";
	private static final String GRAPH_HEIGHT_LONG = "height";
	private static final String GRAPH_FONT_SIZE_LONG = "font-size";

	private static final String PIPELINE_FILE_DESCRIPTION = CommonParameters.PIPELINE_FILE_DESCRIPTION;
	private static final String OUTPUT_FILE_DESCRIPTION = "output file";
	private static final String GRAPH_FORMAT_DESCRIPTION = 
		"graph format. Values: " + getOutputformatValues();
	private static final String GRAPH_WIDTH_DESCRIPTION = "graph width. "
		+ "By default, no width is used so the graph takes the minimum "
		+ "required. This option is incompatible with --"
		+ GRAPH_HEIGHT_LONG;
	private static final String GRAPH_HEIGHT_DESCRIPTION = "graph height. "
		+ "By default, no height is used so the graph takes the minimum " 
		+ "required. This option is incompatible with --"
		+ GRAPH_WITH_LONG;
	private static final String GRAPH_FONT_SIZE_DESCRIPTION = "graph font size";

	private static final String DEFAULT_OUTPUT_FORMAT_STRING = 
		DEFAULT_OUTPUT_FORMAT.toString().toLowerCase();

	@Override
	public void execute(final Parameters parameters) throws Exception {
		File pipelineFile = new File(
			parameters.getSingleValueString(super.getOption(PIPELINE_FILE)));

		if (!pipelineFile.exists()) {
			LOGGER.severe("Pipeline file not found: " + pipelineFile);
			System.exit(1);
		}

		LOGGER.info("Pipeline file - " + pipelineFile);

		File outputFile = new File(
			parameters.getSingleValueString(super.getOption(OUTPUT_FILE)));
		
		LOGGER.info("Export graph to file - " + outputFile);

		if (!outputFile.getParentFile().canWrite()) {
			LOGGER.severe("Can't write output file: " + outputFile);
			System.exit(1);
		}

		PipelineGraphExporterBuilder graphExporterBuilder = 
			new PipelineGraphExporterBuilder(pipelineFile, outputFile);
		
		String graphFormat = 
			parameters.getSingleValueString(super.getOption(GRAPH_FORMAT));

		if (!graphFormat.equals(DEFAULT_OUTPUT_FORMAT_STRING)) {
			LOGGER.info("Graph format - " + graphFormat);
			graphExporterBuilder = graphExporterBuilder.outputFormat(graphFormat);
		}

		Integer graphWidth = parameters
			.getSingleValue(super.getOption(GRAPH_WIDTH));
		Integer graphHeight = parameters
			.getSingleValue(super.getOption(GRAPH_HEIGHT));

		if (graphWidth > 0 && graphHeight > 0) {
			throw new IllegalArgumentException(
				"You can specify " + GRAPH_WITH_LONG + " or "
					+ GRAPH_HEIGHT_LONG + ", but not both at the same time.");
		}

		if (graphWidth > 0) {
			LOGGER.info("Graph width - " + graphWidth);
			graphExporterBuilder = graphExporterBuilder.width(graphWidth);
		} else if (graphHeight > 0) {
			LOGGER.info("Graph height - " + graphHeight);
			graphExporterBuilder = graphExporterBuilder.height(graphHeight);
		}

		Integer fontSize = parameters
			.getSingleValue(super.getOption(GRAPH_FONT_SIZE));

		if (fontSize != DEFAULT_FONT_SIZE) {
			LOGGER.info("Graph font size - " + fontSize);
			graphExporterBuilder = graphExporterBuilder.fontSize(fontSize);
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
		options.add(getWidthOption());
		options.add(getHeightOption());
		options.add(getFontSizeOption());

		return options;
	}

	private StringOption getPipelineFileOption() {
		return new StringOption(PIPELINE_FILE_LONG, PIPELINE_FILE,
			PIPELINE_FILE_DESCRIPTION, false, true, false);
	}

	private StringOption getOutputFileOption() {
		return new StringOption(OUTPUT_FILE_LONG, OUTPUT_FILE,
			OUTPUT_FILE_DESCRIPTION, false, true, false);
	}

	private DefaultValuedStringOption getOutputFormatOption() {
		return new DefaultValuedStringOption(GRAPH_FORMAT_LONG, GRAPH_FORMAT,
			GRAPH_FORMAT_DESCRIPTION, DEFAULT_OUTPUT_FORMAT_STRING);
	}

	private IntegerOption getWidthOption() {
		return new IntegerOption(GRAPH_WITH_LONG, GRAPH_WIDTH,
			GRAPH_WIDTH_DESCRIPTION, DEFAULT_WIDTH);
	}

	private IntegerOption getHeightOption() {
		return new IntegerOption(GRAPH_HEIGHT_LONG, GRAPH_HEIGHT,
			GRAPH_HEIGHT_DESCRIPTION, DEFAULT_HEIGHT);
	}

	private IntegerOption getFontSizeOption() {
		return new IntegerOption(GRAPH_FONT_SIZE_LONG, GRAPH_FONT_SIZE,
			GRAPH_FONT_SIZE_DESCRIPTION, DEFAULT_FONT_SIZE);
	}

	private static final String getOutputformatValues() {
		return asList(OutputFormat.values()).stream()
			.map(OutputFormat::toString).map(String::toLowerCase)
			.collect(joining(", "));
	}
}