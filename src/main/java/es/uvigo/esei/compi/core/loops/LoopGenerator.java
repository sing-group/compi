package es.uvigo.esei.compi.core.loops;

import java.util.List;

/**
 * Gets the values to allow the loop execution
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public interface LoopGenerator {
	/**
	 * Gets the values to allow the loop execution
	 * 
	 * @param source
	 *            Indicates the source to obtain the loop values. It can be
	 *            values split by a comma or a directory
	 * @return A {@link List} with all the values
	 */
	List<String> getValues(String source);
}
