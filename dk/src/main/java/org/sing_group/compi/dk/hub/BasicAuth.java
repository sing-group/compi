/*-
 * #%L
 * Compi Development Kit
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
