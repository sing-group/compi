package org.sing_group.compi.core.loops;

import static org.sing_group.compi.core.runner.ProcessCreator.createShellCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.entities.Foreach;

public class CommandLoopValuesGenerator extends AbstractLoopValuesGenerator {

  public CommandLoopValuesGenerator(VariableResolver resolver, Foreach foreach) {
    super(resolver, foreach);
  }

  @Override
  public List<String> getValuesFromResolvedSource(String source) {
    final List<String> values = new ArrayList<>();
    try {

      Process p = new ProcessBuilder(createShellCommand(source)).start();
      
      Thread t = new Thread(() -> {
        try (Scanner sc = new Scanner(p.getInputStream())) {
          while (sc.hasNextLine()) {
            values.add(sc.nextLine());
          }
        }

      });
      t.start();
      p.waitFor();
      t.join();
      return values;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
