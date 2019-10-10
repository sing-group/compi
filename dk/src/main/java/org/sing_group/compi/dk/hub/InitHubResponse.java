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

import org.json.JSONObject;

public class InitHubResponse {

  private String json;
  private String id;

  public InitHubResponse(String json) {
    this.json = json;
  }

  public String getHubId() {
    if (this.id == null) {
      this.parseJson();
    }
    return this.id;
  }

  private void parseJson() {
    JSONObject jsonResponse = new JSONObject(this.json);
    this.id = jsonResponse.getString("_id");
  }
}
