/*-
 * #%L
 * Compi Development Kit
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
package org.sing_group.compi.dk.cli;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class WriteOnlyProperties extends Properties {
  private static final long serialVersionUID = 1L;

  @Override
  public void store(OutputStream out, String comments) throws IOException {
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
    bw.write("#" + new Date().toString());
    bw.newLine();
    synchronized (this) {
      for (Enumeration<?> e = keys(); e.hasMoreElements();) {
        String key = (String) e.nextElement();
        String val = (String) get(key);
        bw.write(key + "=" + val);
        System.err.println(key + "=" + val);
        bw.newLine();
      }
    }
    bw.flush();
  }
}
