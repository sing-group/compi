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
