/*-
 * #%L
 * Compi Development Kit
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
package org.sing_group.compi.dk.cli;

import static org.sing_group.compi.core.pipeline.Pipeline.fromFile;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_DEFAULT_VALUE;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_DESCRIPTION;
import static org.sing_group.compi.dk.cli.CommonParameters.PIPELINE_FILE_LONG;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.validation.ValidationError;

import es.uvigo.ei.sing.yacli.command.AbstractCommand;
import es.uvigo.ei.sing.yacli.command.option.DefaultValuedStringOption;
import es.uvigo.ei.sing.yacli.command.option.Option;
import es.uvigo.ei.sing.yacli.command.parameter.Parameters;

public class CreateMetadataSkeletonCommand extends AbstractCommand {
  private static final String COMPLETE_REPORT_NAME = "complete-report";
  private static final String COMPLETE_REPORT_SHORT_NAME = "c";
  private static final String COMPLETE_REPORT_DESCRIPTION = "Use this flag to print a complete report";

  public String getName() {
    return "create-metadata-skeleton";
  }

  public String getDescriptiveName() {
    return "Creates metadata skeleton";
  }

  public String getDescription() {
    return "Creates the metadata skeleton based on the current parameters";
  }

  @Override
  protected List<Option<?>> createOptions() {
    return Arrays.asList(
      getPipelineFileOption()
    );
  }

  private Option<?> getPipelineFileOption() {
    return new DefaultValuedStringOption(
      PIPELINE_FILE_LONG, PIPELINE_FILE, PIPELINE_FILE_DESCRIPTION, PIPELINE_FILE_DEFAULT_VALUE
    );
  }

  @Override
  public void execute(Parameters parameters) throws Exception {
    String pipelineFileName = parameters.getSingleValueString(super.getOption(PIPELINE_FILE));

    File pipelineFile = new File(pipelineFileName);

    if (!pipelineFile.exists()) {
      throw new IllegalArgumentException("Pipeline file not found: " + pipelineFile);
    }

    List<ValidationError> errors = new ArrayList<>();

    Pipeline pipeline = fromFile(new File(pipelineFileName), errors);

    createMetadataSkeleton(pipeline);
  }

  private static void createMetadataSkeleton(Pipeline pipeline) {
    System.out.println("\t<metadata>");
    pipeline.getTasks().forEach(t -> {
      System.out.println(String.format("\t\t<task-description id=\"%s\">.</task-description>", t.getId()));
    });
    System.out.println("\t</metadata>");
  }
}
