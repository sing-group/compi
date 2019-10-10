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
