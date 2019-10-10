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
