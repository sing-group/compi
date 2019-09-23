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

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class ImportVersionZip {

  private BasicAuth basicAuth;
  private String id;
  private boolean visible;
  private File zipFile;

  public ImportVersionZip(BasicAuth basicAuth, String id, boolean visible, File zipFile) {
    this.basicAuth = basicAuth;
    this.id = id;
    this.zipFile = zipFile;
    this.visible = visible;
  }

  public void uploadZip() throws ClientProtocolException, IOException, PipelineExistsException {
    CompiHub compiHub = new CompiHub(this.basicAuth);
    HttpPost httpPost = new HttpPost(compiHub.url("version/import"));
    httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.basicAuth.getBasicAuth());

    httpPost.setEntity(buildMultipartEntity());

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(httpPost);

    if (response.getStatusLine().getStatusCode() != 200) {
      if (response.getStatusLine().getStatusCode() == 400) {
        throw new PipelineExistsException();
      } else {
        throw new IOException(EntityUtils.toString(response.getEntity()));
      }
    }

    httpClient.close();
  }

  public void updateZip(CompiHubPipelineVersion version) throws ClientProtocolException, IOException {
    CompiHub compiHub = new CompiHub(this.basicAuth);
    HttpPut httpPut = new HttpPut(compiHub.url("version/import/" + version.getId()));
    httpPut.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + this.basicAuth.getBasicAuth());

    httpPut.setEntity(buildMultipartEntity());

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpResponse response = httpClient.execute(httpPut);

    if (response.getStatusLine().getStatusCode() != 200) {
      throw new IOException(EntityUtils.toString(response.getEntity()));
    }

    httpClient.close();
  }

  private HttpEntity buildMultipartEntity() {
    return MultipartEntityBuilder.create()
      .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
      .addTextBody("visible", this.visible ? "true" : "false")
      .addTextBody("pipeline_id", this.id)
      .addBinaryBody("file", this.zipFile, ContentType.create("application/zip"), this.zipFile.getName())
      .build();
  }
}
