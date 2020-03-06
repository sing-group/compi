/*-
 * #%L
 * Compi Core
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
package org.sing_group.compi.tests;

import static org.junit.Assert.assertTrue;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.core.pipeline.Pipeline.fromFile;
import static org.sing_group.compi.tests.TestUtils.resolverFor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.xml.sax.SAXException;

public class InterpreterTest {

  @SuppressWarnings("resource")
  @Test
  public void testSimpleInterpreterConfiguration() throws IllegalArgumentException,
    PipelineValidationException, IOException, ParserConfigurationException, SAXException, InterruptedException {
    final File inputFile = new File("/tmp/interpreter-input");
    final File outputFileAwk = new File("/tmp/interpreter-output-awk");
    final File outputFilePython = new File("/tmp/interpreter-output-python");

    inputFile.delete();
    outputFileAwk.delete();
    outputFilePython.delete();

    try (PrintStream out = new PrintStream(inputFile)) {
      out.println("line1_1\tline1_2");
      out.println("line2_1\tline2_2");
    }

    final String pipelineFile = ClassLoader.getSystemResource("specificInterpreterPipeline.xml").getFile();

    final CompiApp compi =
      new CompiApp(
        forPipeline(fromFile(new File(pipelineFile)), new File(pipelineFile))
          .whichResolvesVariablesWith(
            resolverFor(
              "input_file", inputFile.getAbsolutePath(),
              "output_file_awk", outputFileAwk.getAbsolutePath(),
              "output_file_python", outputFilePython.getAbsolutePath()
            )
          )
          .build()
      );

    compi.run();

    assertTrue(outputFileAwk.exists());
    assertTrue(outputFilePython.exists());
    String outputFileAwkContents = new Scanner(outputFileAwk).useDelimiter("\\Z").next();
    String outputFilePythonContents = new Scanner(outputFilePython).useDelimiter("\\Z").next();

    assertTrue(outputFileAwkContents.contains("line1_1\tline1_2"));
    assertTrue(outputFileAwkContents.contains("line2_1\tline2_2"));
    assertTrue(outputFilePythonContents.contains(inputFile.getAbsolutePath()));
  }
}
