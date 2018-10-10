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
package org.sing_group.compi.tests;

import static org.junit.Assert.assertTrue;
import static org.sing_group.compi.core.CompiRunConfiguration.forPipeline;
import static org.sing_group.compi.tests.TestUtils.resolverFor;
import static org.sing_group.compi.xmlio.entities.Pipeline.fromFile;

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
        forPipeline(fromFile(new File(pipelineFile)))
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
