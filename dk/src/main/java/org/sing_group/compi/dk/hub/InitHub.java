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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class InitHub {

  private BasicAuth basicAuth;
  private String title;
  private String alias;
  private boolean visible;

  public InitHub(BasicAuth basicAuth, String alias, String title, boolean visible) {
    this.basicAuth = basicAuth;
    this.alias = alias;
    this.title = title;
    this.visible = visible;
  }

  public InitHubResponse initHub() throws ClientProtocolException, IOException, PipelineExistsException {
    CompiHub compiHub = new CompiHub(this.basicAuth);
    HttpPost httpPost = new HttpPost(compiHub.url("pipeline"));
    httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.basicAuth.getBasicAuth());
    httpPost.setHeader("Content-type", "application/json");
    httpPost.setEntity(getEntity());

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(httpPost);

    if (response.getStatusLine().getStatusCode() != 200) {
      if (response.getStatusLine().getStatusCode() == 400) {
        throw new PipelineExistsException();
      } else {
        throw new IOException(EntityUtils.toString(response.getEntity()));
      }
    } else {
      return new InitHubResponse(EntityUtils.toString(response.getEntity()));
    }
  }

  private StringEntity getEntity() throws UnsupportedEncodingException {
    StringBuilder sb = new StringBuilder();
    sb
      .append("{")
      .append(quote("alias"))
      .append(":")
      .append(quote(this.alias))
      .append(",")
      .append(quote("title"))
      .append(":")
      .append(quote(this.title))
      .append(",")
      .append(quote("visible"))
      .append(":")
      .append(quote(this.visible ? "true" : "false"))
      .append(",")
      .append(quote("user_id"))
      .append(":")
      .append(quote(this.basicAuth.getUser()))
      .append("}");

    return new StringEntity(sb.toString());
  }

  private static String quote(String str) {
    return "\"" + str + "\"";
  }
}
