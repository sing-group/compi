package org.sing_group.compi.core.loops;

import org.sing_group.compi.xmlio.entities.Foreach;

/**
 * Auxiliary class to obtain the task command to execute
 * 
 * @author Jesus Alvarez Casanova
 *
 */
public class ForeachIteration {

	private String iterationValue;
  private Foreach task;

	public ForeachIteration(Foreach task) {
	  this.task = task;
	}
	

	public ForeachIteration(Foreach task, final String iterationValue) {
		this.task = task;
		this.iterationValue = iterationValue;
	}

	public Foreach getTask() {
    return task;
  }
	
	public String getIterationValue() {
		return iterationValue;
	}

}
