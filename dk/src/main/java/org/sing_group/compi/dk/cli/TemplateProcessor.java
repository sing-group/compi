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
