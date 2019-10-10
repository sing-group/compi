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
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

public class CompiHub {

  private BasicAuth basicAuth;

  public CompiHub(BasicAuth basicAuth) {
    this.basicAuth = basicAuth;
  }

  public String url(String path) {
    return System.getProperty("compihub.backend.url", "https://sing-group.org/compihub-backend/") + path;
  }

  public List<CompiHubPipeline> listUserPipelines() throws ClientProtocolException, IOException {
    HttpGet httpGet = new HttpGet(url("pipeline/user/" + this.basicAuth.getUser()));
    httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.basicAuth.getBasicAuth());

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(httpGet);

    if (response.getStatusLine().getStatusCode() == 200) {
      return this.parsePipelineListResponse(response.getEntity());
    } else {
      throw new IOException(EntityUtils.toString(response.getEntity()));
    }
  }

  private List<CompiHubPipeline> parsePipelineListResponse(HttpEntity entity)
    throws JSONException, ParseException, IOException {
    List<CompiHubPipeline> pipelines = new LinkedList<>();
    JSONArray jsonResponse = new JSONArray(EntityUtils.toString(entity));

    for (int i = 0; i < jsonResponse.length(); i++) {
      pipelines.add(new CompiHubPipeline(jsonResponse.getJSONObject(i).toString()));
    }

    return pipelines;
  }

  public List<CompiHubPipelineVersion> listPipelineVersions(String pipelineId)
    throws ClientProtocolException, IOException {
    HttpGet httpGet = new HttpGet(url("version?pipeline=" + pipelineId));
    httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.basicAuth.getBasicAuth());

    HttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(httpGet);

    if (response.getStatusLine().getStatusCode() == 200) {
      return this.parsePipelineVersionsResponse(response.getEntity());
    } else {
      throw new IOException(EntityUtils.toString(response.getEntity()));
    }
  }

  private List<CompiHubPipelineVersion> parsePipelineVersionsResponse(HttpEntity entity)
    throws JSONException, ParseException, IOException {
    List<CompiHubPipelineVersion> versions = new LinkedList<>();
    JSONArray jsonResponse = new JSONArray(EntityUtils.toString(entity));

    for (int i = 0; i < jsonResponse.length(); i++) {
      versions.add(new CompiHubPipelineVersion(jsonResponse.getJSONObject(i).toString()));
    }

    return versions;
  }
}
