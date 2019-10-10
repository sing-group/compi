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
package org.sing_group.compi.core.loops;

import static org.sing_group.compi.core.runner.ProcessCreator.createShellCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.resolver.VariableResolver;

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
