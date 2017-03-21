package org.sing_group.compi.xmlio;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sing_group.compi.xmlio.entities.Program;

/**
 * Obtains the content of the {@link Program} exec tag
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class PipelineParser {

	private static Pattern pattern;
	private static Matcher matcher;

	/**
	 * Obtain the content inside ${...} in the {@link Program} exec tag
	 * 
	 * @param programs
	 *            Contains all the {@link Program}
	 */
	public static void solveExec(final List<Program> programs) {
		for (final Program program : programs) {
			pattern = Pattern.compile("\\{(.*?)\\}");
			matcher = pattern.matcher(program.getExec());
			while (matcher.find()) {
				program.getExecStrings().add(matcher.group(1));
			}
		}
	}

}
