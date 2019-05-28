/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2019 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
