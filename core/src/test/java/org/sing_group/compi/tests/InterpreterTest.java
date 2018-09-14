package org.sing_group.compi.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.sing_group.compi.core.CompiApp;
import org.sing_group.compi.core.PipelineValidationException;
import org.xml.sax.SAXException;

public class InterpreterTest {

  @SuppressWarnings("resource")
  @Test
  public void testSimpleInterpreterConfiguration() throws IllegalArgumentException, JAXBException,
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
        pipelineFile, 1, TestUtils.resolverFor(
          "input_file", inputFile.getAbsolutePath(), "output_file_awk", outputFileAwk.getAbsolutePath(), "output_file_python", outputFilePython.getAbsolutePath()
        ), null, null, null, null, null
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
