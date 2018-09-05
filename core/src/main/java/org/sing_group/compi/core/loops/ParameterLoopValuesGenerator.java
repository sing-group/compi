package org.sing_group.compi.core.loops;

import static java.util.Arrays.asList;

import java.util.List;

import org.sing_group.compi.core.VariableResolver;

public class ParameterLoopValuesGenerator extends AbstractLoopValuesGenerator {

	public ParameterLoopValuesGenerator(VariableResolver resolver) {
		super(resolver);
	}
	
	@Override
	public List<String> getValues(String source) {
		String variableValue = resolver.resolveVariable(source);
		if (variableValue == null) {
			throw new IllegalArgumentException("Variable "+source+" is not defined");
		}
		return asList(resolver.resolveVariable(source).split(","));
	}

  @Override
  protected List<String> getValuesFromResolvedSource(String source) { return null; }
}
