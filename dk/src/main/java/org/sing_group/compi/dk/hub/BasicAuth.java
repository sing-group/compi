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
package org.sing_group.compi.dk.hub;

import java.io.Console;
import java.util.Base64;

public class BasicAuth {

  private String basicAuth;
  private String user;

  public BasicAuth(String user, String password) {
    this.basicAuth = Base64.getEncoder().encodeToString(((user + ":" + password).getBytes()));
    this.user = user;
  }

  public String getUser() {
    return user;
  }
  
  public String getBasicAuth() {
    return basicAuth;
  }

  public static BasicAuth fromConsole(Console console) {
    String username = console.readLine("Username: ");
    String password = new String(console.readPassword("Password: "));

    return new BasicAuth(username, password);
  }
}
