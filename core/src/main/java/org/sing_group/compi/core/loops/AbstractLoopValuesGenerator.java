package org.sing_group.compi.core.loops;

import java.util.List;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.core.resolver.VariableResolverUtils;
import org.sing_group.compi.xmlio.entities.Foreach;

public abstract class AbstractLoopValuesGenerator implements LoopValuesGenerator {

  protected VariableResolver resolver;
  private Foreach foreach;
  
  public AbstractLoopValuesGenerator(VariableResolver resolver, Foreach foreach) {
    this.resolver = resolver;
    this.foreach = foreach;
  }
  
  @Override
  public List<String> getValues(String source) {
    source = resolveCommandParameters(source);
    return this.getValuesFromResolvedSource(source);
  }
  
  protected abstract List<String> getValuesFromResolvedSource(String source);
  
  private String resolveCommandParameters(String source) {
    VariableResolverUtils resolverUtils = new VariableResolverUtils(this.resolver);
    String resolvedString = resolverUtils.resolveAllVariables(source, foreach);
    return resolvedString;
  }
}
