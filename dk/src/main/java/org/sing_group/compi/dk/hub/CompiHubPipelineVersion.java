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

import org.json.JSONObject;

public class CompiHubPipelineVersion {

  private String json;
  private boolean parseJson = true;

  private String version;
  private String id;

  public CompiHubPipelineVersion(String json) {
    this.json = json;
  }

  public String getVersion() {
    if (this.parseJson) {
      this.parseJson();
    }
    return this.version;
  }

  public String getId() {
    if (this.parseJson) {
      this.parseJson();
    }
    return this.id;
  }

  private void parseJson() {
    JSONObject jsonObject = new JSONObject(this.json);
    this.id = jsonObject.getString("_id");
    this.version = jsonObject.getString("version");
  }

  @Override
  public String toString() {
    return "Id: " + this.getId() + "; Version: " + this.getVersion();
  }
}
