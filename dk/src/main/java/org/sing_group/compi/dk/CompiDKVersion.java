package org.sing_group.compi.dk;

import java.io.IOException;
import java.util.Properties;

public class CompiDKVersion {
	public static String getCompiDKVersion() {
		try {
			Properties p = new Properties();
			p.load(
				CompiDKVersion.class.getResourceAsStream("/compi-dk.version"));
			return p.getProperty("compi-dk.version");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
