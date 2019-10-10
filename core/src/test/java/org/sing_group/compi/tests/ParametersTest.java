/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;

import org.junit.Test;
import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.ParamsFileVariableResolver;

public class ParametersTest {

  @Test
  public void testParametersFileLoading() {
    final String paramsFile = ClassLoader.getSystemResource("paramsFile").getFile();

    VariableResolver variableResolver = new ParamsFileVariableResolver(new File(paramsFile));

    assertEquals(new HashSet<>(asList("a", "b", "c", "d_equal", "e_empty")), variableResolver.getVariableNames());
    assertEquals("tmp/testingFile.txt", variableResolver.resolveVariable("a"));
    assertEquals("touch", variableResolver.resolveVariable("b"));
    assertEquals("10", variableResolver.resolveVariable("c"));
    assertEquals("at=ta", variableResolver.resolveVariable("d_equal"));
    assertEquals("", variableResolver.resolveVariable("e_empty"));
  }
}
