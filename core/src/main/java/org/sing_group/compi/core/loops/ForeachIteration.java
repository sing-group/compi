package org.sing_group.compi.core.loops;

import org.sing_group.compi.xmlio.entities.Foreach;

/**
 * Auxiliary class to obtain the task command to execute
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ForeachIteration extends Foreach {

	private String iterationValue;
	private Foreach parentForeachTask;

	public static ForeachIteration createIterationForForeach(Foreach foreach, String iterationValue) {
		ForeachIteration foreachIteration = cloneForeach(foreach);
		
		foreachIteration.parentForeachTask = foreach;
		foreachIteration.iterationValue = iterationValue;
		
		return foreachIteration;
	}

	private static ForeachIteration cloneForeach(Foreach foreach) {
		ForeachIteration foreachIteration = new ForeachIteration();
		
		foreachIteration.setId(foreach.getId());
		foreachIteration.setFileErrorLog(foreach.getFileErrorLog());
		foreachIteration.setFileLog(foreach.getFileLog());
		foreachIteration.setFinished(foreach.isFinished());
		foreachIteration.setAborted(foreach.isAborted());
		foreachIteration.setInterpreter(foreach.getInterpreter());
		foreachIteration.setParametersString(foreach.getParametersString());
		foreach.getParameters().forEach((param) -> foreachIteration.addParameter(param));
		foreachIteration.setRunning(foreach.isRunning());
		foreachIteration.setToExecute(foreach.getToExecute());
		foreachIteration.setExec(foreach.getExec());
		foreachIteration.setSkipped(foreach.isSkipped());
		foreachIteration.setIn(foreach.getIn());
		foreachIteration.setAs(foreach.getAs());
		foreachIteration.setOf(foreach.getOf());
		return foreachIteration;
	}

	public Foreach getParentForeachTask() {
		return parentForeachTask;
	}

	public String getIterationValue() {
		return iterationValue;
	}

}
