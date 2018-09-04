package org.sing_group.compi.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextVariableResolver {
  
  private VariableResolver resolver;
  
  public TextVariableResolver(VariableResolver resolver) {
    this.resolver = resolver;
  }
  
  public String resolveAllVariables(String text) {
    final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
    String resolvedString = text;
    final Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String varName = matcher.group(1);
      String resolvedVarName = this.resolver.resolveVariable(varName);
      if (resolvedVarName != null) {
        resolvedString = resolvedString.replaceFirst("\\$\\{"+varName+"\\}", resolvedVarName);
      }
    }
    return resolvedString; 
  }
}
