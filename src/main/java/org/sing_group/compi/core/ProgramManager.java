package org.sing_group.compi.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sing_group.compi.core.loops.VarLoopGenerator;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;
import org.sing_group.compi.core.loops.FileLoopGenerator;
import org.sing_group.compi.core.loops.LoopProgram;

/**
 * Manages the {@link Program} execution and manages the {@link Program}
 * dependencies
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ProgramManager implements ProgramExecutionHandler {

	private final ProgramExecutionHandler handler;
	private final Map<String, Program> DAG = new ConcurrentHashMap<>();
	private final List<String> programsLeft = new CopyOnWriteArrayList<>();
	private final List<Program> runnablePrograms = new CopyOnWriteArrayList<>();
	private final Map<String, Set<String>> dependencies = new ConcurrentHashMap<>();
	private final Map<String, List<LoopProgram>> forEachPrograms = new ConcurrentHashMap<>();
	private boolean firstExecution;

	/**
	 * @param handler
	 *            Indicates the {@link ProgramExecutionHandler}
	 * @param pipeline
	 *            Indicates the {@link Pipeline}
	 */
	public ProgramManager(final ProgramExecutionHandler handler, final Pipeline pipeline) {
		this.handler = handler;
		for (final Program p : pipeline.getPrograms()) {
			this.DAG.put(p.getId(), p);
			this.programsLeft.add(p.getId());
			this.dependencies.put(p.getId(), new HashSet<String>());

			if (p.getForeach() != null) {
				this.forEachPrograms.put(p.getId(), new LinkedList<LoopProgram>());
			}
		}
		this.firstExecution = true;
	}

	/**
	 * Returns a {@link List} of {@link Program} ready to run. The first time,
	 * it will check if the {@link Program} has dependencies or not. If the
	 * {@link Program} doesn't have any dependency it can be executed, otherwise
	 * the {@link Program} must wait until its dependencies has been finished
	 * 
	 * @return A {@link List} of {@link Program} ready to run
	 */
	public List<Program> getRunnablePrograms() {
		this.runnablePrograms.clear();
		if (this.firstExecution) {
			this.firstExecution = false;
			DAG.forEach((key, value) -> {
				if (value.getDependsOn() == null) {
					this.runnablePrograms.add(value);
				}
			});
		} else {
			for (final String programId : this.programsLeft) {
				final Program program = DAG.get(programId);
				if (checkProgramDependencies(program)) {
					this.runnablePrograms.add(program);
				}
			}
		}
		return runnablePrograms;
	}

	/**
	 * Goes through all the {@link Program} dependencies to check if they are
	 * finished
	 * 
	 * @param program
	 *            Indicates the {@link Program}
	 * @return <code>true</code> if all its dependencies are finished,
	 *         <code>false</code> otherwise
	 */
	private boolean checkProgramDependencies(final Program program) {
		int count = 0;
		final String[] dependsArray = program.getDependsOn().split(",");
		for (final String s : dependsArray) {
			final Program programToCheck = DAG.get(s);
			if (programToCheck.isFinished()) {
				count++;
			}
		}
		if (count == dependsArray.length) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Verifies that exist all the IDs contained in the {@link Program}
	 * attribute "dependsOn"
	 * 
	 * @throws IllegalArgumentException
	 *             If the {@link Program} ID contained in the dependsOn
	 *             attribute doesn't exist
	 */
	public void checkDependsOnIds() throws IllegalArgumentException {
		for (final String programs : this.programsLeft) {
			final Program program = DAG.get(programs);
			if (program.getDependsOn() != null) {
				for (final String dependsOn : program.getDependsOn().split(",")) {
					if (!DAG.containsKey(dependsOn)) {
						throw new IllegalArgumentException(
								"The IDs contained in the dependsOn attribute of the program " + program.getId()
										+ " aren't correct");
					}
				}
			}
		}
	}

	/**
	 * Creates all the {@link LoopProgram} to prepare the loop execution
	 * 
	 * @throws IllegalArgumentException
	 *             If the directory contained in the source attribute doesn't
	 *             have any file or if the element attribute contains a non
	 *             existent value
	 */
	public void initializeForEach() throws IllegalArgumentException {
		if (!this.getForEachPrograms().isEmpty()) {
			this.getForEachPrograms().forEach((key, value) -> {
				final Program program = this.DAG.get(key);
				List<String> values = new LinkedList<>();
				switch (program.getForeach().getElement()) {
				case "var":
					final VarLoopGenerator vlg = new VarLoopGenerator();
					values = vlg.getValues(program.getForeach().getSource());
					break;
				case "file":
					final FileLoopGenerator flg = new FileLoopGenerator();
					values = flg.getValues(program.getForeach().getSource());
					if (values.isEmpty()) {
						throw new IllegalArgumentException("The directory " + program.getForeach().getSource()
								+ " of the program " + program.getId() + " doesn't contain any		 file");
					}
					break;
				default:
					throw new IllegalArgumentException("The element " + program.getForeach().getElement()
							+ " of the program " + key + " doesn't exist");
				}

				for (final String source : values) {
					value.add(new LoopProgram(program.getExec(), source, program.getForeach().getAs()));
				}
			});
		}
	}

	/**
	 * Initializes the {@link Program} dependencies. First it will check the
	 * {@link Program} dependencies and then it will check if a {@link Program}
	 * it's a dependency of another {@link Program}
	 */
	public void initializeDependencies() {
		DAG.forEach((key, value) -> {
			if (value.getDependsOn() != null) {
				for (final String dependsOn : value.getDependsOn().split(",")) {
					dependencies.get(dependsOn).add(key);
				}
			}
		});

		DAG.forEach((key, value) -> {
			dependencies.forEach((key2, value2) -> {
				if (value2.contains(key)) {
					value2.addAll(dependencies.get(key));
				}
			});
		});
	}

	/**
	 * Marks all the {@link Program} as skipped if they are dependencies of the
	 * {@link Program} passed as parameter
	 * 
	 * @param advanceTo
	 *            Indicates the {@link Program} ID which you want to advance
	 */
	public void skipPrograms(final String advanceTo) {
		dependencies.forEach((key, value) -> {
			if (value.contains(advanceTo)) {
				this.getDAG().get(key).setSkipped(true);
			}
		});
	}

	/**
	 * Marks a {@link Program} as started
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	@Override
	public void programStarted(final Program program) {
		this.getDAG().get(program.getId()).setRunning(true);
		this.getProgramsLeft().remove(program.getId());
	}

	/**
	 * Marks a {@link Program} as finished
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	@Override
	public void programFinished(final Program program) {
		this.getDAG().get(program.getId()).setFinished(true);
		this.getDAG().get(program.getId()).setRunning(false);
	}

	/**
	 * Marks a {@link Program} as aborted and aborts all the {@link Program}
	 * which has as dependency of the aborted {@link Program}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	@Override
	public void programAborted(final Program program, final Exception e) {
		this.getDAG().get(program.getId()).setAborted(true);
		this.getDAG().get(program.getId()).setRunning(false);
		for (final String programToAbort : this.getDependencies().get(program.getId())) {
			if (this.getProgramsLeft().contains(programToAbort)) {
				handler.programAborted(this.getDAG().get(programToAbort), e);
				this.getProgramsLeft().remove(programToAbort);
			}
		}
	}

	/**
	 * Getter of the DAG global variable
	 * 
	 * @return The value of the DAG global variable
	 */
	public Map<String, Program> getDAG() {
		return DAG;
	}

	/**
	 * Getter of the programsLeft global variable
	 * 
	 * @return The value of the programsLeft global variable
	 */
	public List<String> getProgramsLeft() {
		return programsLeft;
	}

	/**
	 * Getter of the dependencies global variable
	 * 
	 * @return The value of the dependencies global variable
	 */
	public Map<String, Set<String>> getDependencies() {
		return dependencies;
	}

	/**
	 * Getter of the forEachPrograms global variable
	 * 
	 * @return The value of the forEachPrograms global variable
	 */
	public Map<String, List<LoopProgram>> getForEachPrograms() {
		return forEachPrograms;
	}

}
