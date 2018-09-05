package org.sing_group.compi.core.loops;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.sing_group.compi.core.VariableResolver;

public class CommandLoopValuesGenerator extends AbstractLoopValuesGenerator {

	public CommandLoopValuesGenerator(VariableResolver resolver) {
		super(resolver);
	}

	@Override
	public List<String> getValuesFromResolvedSource(String source) {
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
}
