/*-
 * #%L
 * Compi Core
 * %%
 * Copyright (C) 2016 - 2018 Daniel Glez-Peña, Osvaldo Graña-Castro, Hugo
 * 			López-Fernández, Jesús Álvarez Casanova
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  private static final long serialVersionUID = 1L;

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
    foreachIteration.setAborted(foreach.isAborted(), foreach.getAbortionCause());
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
