package org.sing_group.compi.core.loops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sing_group.compi.core.VariableResolver;

public class CommandLoopValuesGenerator implements LoopValuesGenerator {

	private VariableResolver resolver;

	public CommandLoopValuesGenerator(VariableResolver resolver) {
		super();
		this.resolver = resolver;
	}

	@Override
	public List<String> getValues(String source) {
		source = resolveCommandParameters(source);
		final List<String> values = new ArrayList<>();
		try {

			Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", source });
			Thread t = new Thread(() -> {
				try (Scanner sc = new Scanner(p.getInputStream())) {
					while (sc.hasNextLine()) {
						values.add(sc.nextLine());
					}
				}

			});
			t.start();
			p.waitFor();
			t.join();
			return values;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String resolveCommandParameters(String command) {
		final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		String resolvedString = command;
		final Matcher matcher = pattern.matcher(command);
		while (matcher.find()) {
			String varName = matcher.group(1);
			String resolvedVarName = resolver.resolveVariable(varName);
			if (resolvedVarName == null) {
				throw new IllegalArgumentException(
						"Variable " + varName + " cannot be resolved in command line " + command);
			}
			resolvedString = resolvedString.replace("${" + varName + "}", resolver.resolveVariable(varName));
		}
		return resolvedString;

	}

}
