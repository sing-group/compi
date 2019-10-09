/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.sing_group.compi.core.loops;

import org.sing_group.compi.core.pipeline.Foreach;
import org.sing_group.compi.core.pipeline.Pipeline;

/**
 * Auxiliary class to obtain the task command to execute
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ForeachIteration extends Foreach {

	private String iterationValue;
	private int iterationIndex;
	private Foreach parentForeachTask;
	
	public ForeachIteration(Pipeline pipeline) {
	  super(pipeline);
	}
	
	public static ForeachIteration createIterationForForeach(Foreach foreach, String iterationValue, int iterationIndex) {
		ForeachIteration foreachIteration = cloneForeach(foreach);
		
		foreachIteration.parentForeachTask = foreach;
		foreachIteration.iterationValue = iterationValue;
		foreachIteration.iterationIndex = iterationIndex;
		
		return foreachIteration;
	}

	private static ForeachIteration cloneForeach(Foreach foreach) {
		ForeachIteration foreachIteration = new ForeachIteration(foreach.getPipeline());
		
		foreachIteration.setId(foreach.getId());
		foreachIteration.setFinished(foreach.isFinished());
		foreachIteration.setAborted(foreach.isAborted());
		foreachIteration.setInterpreter(foreach.getInterpreter());
		foreachIteration.setRunIf(foreach.getRunIf());
		foreachIteration.setParametersString(foreach.getParametersString());
		foreach.getParameters().forEach((param) -> foreachIteration.addParameter(param));
		foreachIteration.setRunning(foreach.isRunning());
		foreachIteration.setToExecute(foreach.getToExecute());
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
	
	public int getIterationIndex() {
		return iterationIndex;
	}

}
