package org.sing_group.compi.core.loops;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Obtains the values of the task foreach tag when the element
 * attribute contains "file"
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class FileLoopValuesGenerator implements LoopValuesGenerator {

	private final List<String> toExecute;

	public FileLoopValuesGenerator() {
		this.toExecute = new LinkedList<>();
	}

	/**
	 * Splits all the values in the task source tag
	 * 
	 * @param source
	 *            Indicates the directory of the task source tag
	 */
	@Override
	public List<String> getValues(final String source) {
		final File folder = new File(source);
		listFilesForFolder(folder);
		return this.toExecute;
	}

	/**
	 * List all the files inside a folder
	 * 
	 * @param folder
	 *            Indicates the folder where the files are searched
	 */
	public void listFilesForFolder(final File folder) {
		if (!folder.exists()) {
			throw new IllegalArgumentException("The folder " + folder + " doesn't exist");
		}
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				this.toExecute.add(fileEntry.getAbsolutePath());
			}
		}
	}
}
