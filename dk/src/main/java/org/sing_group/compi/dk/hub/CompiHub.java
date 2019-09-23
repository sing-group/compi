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
