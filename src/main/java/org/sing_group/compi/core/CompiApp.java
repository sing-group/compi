package org.sing_group.compi.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.sing_group.compi.core.loops.LoopProgram;
import org.sing_group.compi.xmlio.DOMparsing;
import org.sing_group.compi.xmlio.PipelineParser;
import org.sing_group.compi.xmlio.XMLParamsFileVariableResolver;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Program;
import org.xml.sax.SAXException;

/**
 * Executes all the {@link Program} contained in the {@link Pipeline}
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class CompiApp implements ProgramExecutionHandler {

	private Pipeline pipeline;
	private ProgramManager programManager;
	private VariableResolver resolver;
	private ExecutorService executorService;
	final String pipelineFile;
	String paramsFile;
	String threadNumber;
	String advanceToProgram;
	private final Map<Program, Program> parentProgram = new HashMap<>();
	private final Map<Program, AtomicInteger> loopCount = new HashMap<>();
	private final List<ProgramExecutionHandler> executionHandlers = new ArrayList<>();

	/**
	 * Constructs the CompiApp
	 * 
	 * @param pipelineFile
	 *            the pipeline file
	 * @param threadNumber
	 * 			  the size of the thread pool
	 * @param resolver
	 * 			  an object to resolve variables
	 * @param advanceToProgram
	 *            the program to start the pipeline from, all previous dependencies are skipped
	 * @param singleProgram
	 *            run a single pipeline program
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws JAXBException
	 *             If there is an error in the XML unmarshal process
	 * @throws ParserConfigurationException If there is an error parsing the pipeline 
	 */

	public CompiApp(final String pipelineFile,final int threadNumber, final VariableResolver resolver, final String advanceToProgram,
			final String singleProgram) throws SAXException, IOException, JAXBException, ParserConfigurationException {
		this.pipelineFile = pipelineFile;
		final File xsdFile = new File(getClass().getClassLoader().getResource("xsd/pipeline.xsd").getFile());
		DOMparsing.validateXMLSchema(pipelineFile, xsdFile);
		this.pipeline = PipelineParser.parsePipeline(new File(this.pipelineFile));
		this.resolver = resolver;
		
		this.programManager = new ProgramManager(this, this.pipeline, this.resolver);
		initializePipeline(advanceToProgram, singleProgram);
		initializeExecutorService(threadNumber);
	}
	
	public CompiApp(final String pipelineFile,final int threadNumber, String paramsFile, final String advanceToProgram,
			final String singleProgram) throws IllegalArgumentException, SAXException, IOException, JAXBException, ParserConfigurationException {
		this(pipelineFile, threadNumber,new XMLParamsFileVariableResolver(paramsFile), advanceToProgram, singleProgram);
	}

	/**
	 * Executes all the {@link Program} in an {@link ExecutorService}. When a
	 * {@link Program} is executed, this thread will wait until the {@link Program}
	 * notifies when it's finished or aborted
	 * 
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws IllegalArgumentException
	 *             If there is an error in the XML pipeline/params file
	 * @throws InterruptedException
	 *             If there is an error while the thread is waiting
	 * @throws ParserConfigurationException
	 *             If there is a configuration error
	 */
	public void run() throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException, InterruptedException  {
		
		synchronized (this) {
			while (!programManager.getProgramsLeft().isEmpty()) {
				for (final Program programToRun : programManager.getRunnablePrograms()) {
					this.programStarted(programToRun);
					if (programHasForEach(programToRun)) {
						programManager.initializeForEach(programToRun);
						if (!programToRun.isSkipped()) resolveProgram(programToRun);
						loopCount.put(programToRun, new AtomicInteger(
								programManager.getForEachPrograms().get(programToRun.getId()).size()));
						for (final LoopProgram lp : programManager.getForEachPrograms().get(programToRun.getId())) {
							final Program cloned = programToRun.clone();
							parentProgram.put(cloned, programToRun);
							cloned.setToExecute(lp.getToExecute());
							executorService.submit(new ProgramRunnable(cloned, this));
						}
					} else {
						if (!programToRun.isSkipped()) resolveProgram(programToRun);
						executorService.submit(new ProgramRunnable(programToRun, this));
					}
				}
				this.wait();
			}
		}

		executorService.shutdown();

		// wait for remaining programs that are currently running
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	}

	/**
	 * Adds a {@link ProgramExecutionHandler}
	 * 
	 * @param handler
	 *            Indicates the {@link ProgramExecutionHandler}
	 */
	public void addProgramExecutionHandler(final ProgramExecutionHandler handler) {
		this.executionHandlers.add(handler);
	}

	/**
	 * Checks if the {@link Program} has the foreach tag
	 * 
	 * @param programToRun
	 *            the {@link Program} to check
	 * @return <code>true</code> if the {@link Program} has a foreach tag,
	 *         <code>false</code> otherwise
	 */
	private boolean programHasForEach(final Program programToRun) {
		return programToRun.getForeach() != null;
	}

	/**
	 * Initializes all the parameters to allow the {@link Program} execution
	 * 
	 * @param advanceToProgram
	 *            Indicates the {@link Program} ID which you want to advance
	 * @throws JAXBException
	 *             If there is an error in the XML unmarshal process
	 * @throws IllegalArgumentException
	 *             If the {@link XMLParamsFileVariableResolver} can't replace a tag
	 * @throws ParserConfigurationException
	 *             If there is a configuration error
	 * @throws IOException
	 *             If an I/O exception of some sort has occurred
	 * @throws SAXException
	 *             If there is an error in the XML parsing
	 */
	private void initializePipeline(final String advanceToProgram,
			final String singleProgram)
			throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		if (advanceToProgram != null && singleProgram != null) {
			throw new IllegalArgumentException("advanceToProgram or singleProgram must be null");
		}
		
		programManager.checkDependsOnIds();
		// PipelineParser.solveExec(pipeline.getPrograms());
		programManager.initializeDependencies();
		
		if (advanceToProgram != null) {
			skipPrograms(advanceToProgram);
		}
		if (singleProgram != null) {
			skipAllBut(singleProgram);
		}

	}

	/**
	 * Initializes the {@link ExecutorService}
	 * 
	 * @param threadNumber
	 *            Indicates the number of threads of the {@link ExecutorService}
	 * @throws IllegalArgumentException
	 *             If the number of threads is equal or less than 0 or if the number
	 *             is a string instead of a number
	 */
	private void initializeExecutorService(final int threadNumber) throws IllegalArgumentException {
		if (threadNumber <= 0) {
			throw new IllegalArgumentException("The thread number must be higher than 0");
		} else {
			executorService = Executors.newFixedThreadPool(threadNumber);
		}
	}

	/**
	 * Skips {@link Program} until the {@link Program} where you want to start
	 * 
	 * @param advanceToProgram
	 *            Indicates the {@link Program} ID
	 * @throws IllegalArgumentException
	 *             If the {@link Program} ID doesn't exist
	 */
	private void skipPrograms(final String advanceToProgram) throws IllegalArgumentException {
		if (!programManager.getProgramsLeft().contains(advanceToProgram)) {
			throw new IllegalArgumentException("The program ID " + advanceToProgram + " doesn't exist");
		} else {
			this.getProgramManager().skipPrograms(advanceToProgram);
		}
	}

	/**
	 * Skips all {@link Program} but {@link Program}
	 * 
	 * @param singleProgram
	 *            Indicates the {@link Program} ID
	 * @throws IllegalArgumentException
	 *             If the {@link Program} ID doesn't exist
	 */
	private void skipAllBut(String singleProgram) {
		if (!programManager.getProgramsLeft().contains(singleProgram)) {
			throw new IllegalArgumentException("The program ID " + singleProgram + " doesn't exist");
		} else {
			this.getProgramManager().skipAllProgramsBut(singleProgram);
		}

	}

	/**
	 * Resolves the command to execute of a given program
	 *
	 * @param p
	 *            The program to resolve
	 * 
	 * @throws IllegalArgumentException
	 *             If the {@link Program} attribute "as" isn't contained in the exec
	 *             tag
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private void resolveProgram(Program p)
			throws IllegalArgumentException, ParserConfigurationException, SAXException, IOException {
		
		if (programHasForEach(p)) {
			if (p.getParameters().contains(p.getForeach().getAs())) {
				for (final LoopProgram lp : programManager.getForEachPrograms().get(p.getId())) {
					for (final String tag : p.getParameters()) {
						if (lp.getAs().equals(tag)) {
							lp.setToExecute(lp.getToExecute().replace("${" + tag + "}", lp.getSource()));
						} else {
							final String parsed = resolver.resolveVariable(tag);
							lp.setToExecute(lp.getToExecute().replace("${" + tag + "}", parsed));
						}
					}
				}
			} else {
				throw new IllegalArgumentException(
						"The as attribute of the program " + p.getId() + " ins't contained in the exec tag");
			}
		} else {
			for (final String parameter : p.getParameters()) {
				final String variableValue = resolver.resolveVariable(parameter);
				p.setToExecute(p.getToExecute().replace("${" + parameter + "}", variableValue));
			}
		}
	}

	/**
	 * Indicates that a {@link Program} is started
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	@Override
	public void programStarted(final Program program) {
		if (!program.isSkipped())
			this.notifyProgramStarted(program);
		this.getProgramManager().programStarted(program);
	}

	/**
	 * Indicates that a {@link Program} is started to an external
	 * {@link ProgramExecutionHandler}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	private void notifyProgramStarted(final Program program) {
		for (final ProgramExecutionHandler handler : this.executionHandlers) {
			handler.programStarted(program);
		}
	}

	/**
	 * Indicates that a {@link Program} is finished and notifies
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been started
	 */
	@Override
	synchronized public void programFinished(final Program program) {
		if (programHasForEach(program)) {
			final Program parent = this.parentProgram.get(program);
			if (loopCount.get(parent).decrementAndGet() == 0) {
				this.getProgramManager().programFinished(parent);
				this.notify();
			}
		} else {
			this.getProgramManager().programFinished(program);
			this.notify();
		}
		if (!program.isSkipped()) {
			this.notifyProgramFinished(program);
		}
	}

	/**
	 * Indicates that a {@link Program} is finished to an external
	 * {@link ProgramExecutionHandler}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been finished
	 */
	private void notifyProgramFinished(final Program program) {
		for (final ProgramExecutionHandler handler : this.executionHandlers) {
			handler.programFinished(program);
		}
	}

	/**
	 * Indicates that a {@link Program} is aborted and notifies
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been aborted
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	@Override
	synchronized public void programAborted(final Program program, final Exception e) {
		this.notifyProgramAborted(program, e);
		this.getProgramManager().programAborted(program, e);
		this.notify();
	}

	/**
	 * Indicates that a {@link Program} is aborted to an external
	 * {@link ProgramExecutionHandler}
	 * 
	 * @param program
	 *            Indicates the {@link Program} which has been aborted
	 * 
	 * @param e
	 *            Indicates the {@link Exception} which causes the error
	 */
	private void notifyProgramAborted(final Program program, final Exception e) {
		for (final ProgramExecutionHandler handler : this.executionHandlers) {
			handler.programAborted(program, e);
		}
	}

	/**
	 * Getter of the programManager attribute
	 * 
	 * @return The value of the programManager attribute
	 */
	public ProgramManager getProgramManager() {
		return programManager;
	}

	/**
	 * Getter of the parentProgram attribute
	 * 
	 * @return The value of the parentProgram attribute
	 */
	public Map<Program, Program> getParentProgram() {
		return parentProgram;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}
	
	public void setResolver(VariableResolver resolver) {
		this.resolver = resolver;
	}
	

}