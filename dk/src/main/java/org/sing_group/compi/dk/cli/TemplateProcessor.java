/*-
 * #%L
 * Compi Development Kit
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
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
package org.sing_group.compi.dk.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class TemplateProcessor {
  public void processTemplate(String templateName, Map<String, String> variables, File destFile) throws IOException {
    Template dockerFileTemplate = getVelocityTemplate(templateName);
    VelocityContext context = new VelocityContext();
    variables.forEach((k, v) -> {
      context.put(k, v);
    });

    try (Writer out = new OutputStreamWriter(new FileOutputStream(destFile))) {
      dockerFileTemplate.merge(context, out);
    } catch (FileNotFoundException e) {
      // this should no happen, since the file must not exist because we have
      // created
      // the directory
      e.printStackTrace();
    }
  }

  private Template getVelocityTemplate(String templateName) {
    VelocityEngine ve = new VelocityEngine();
    ve.setProperty("resource.loader", "class");
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    ve.init();
    Template dockerFileTemplate =
      ve.getTemplate(
        File.separator
          + getClass().getPackage().getName().replace('.', File.separatorChar) + File.separator + templateName
      );
    return dockerFileTemplate;
  }

}
