package org.sing_group.compi.core.runner;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sing_group.compi.core.resolver.VariableResolver;
import org.sing_group.compi.xmlio.entities.Pipeline;
import org.sing_group.compi.xmlio.entities.Task;
import org.sing_group.compi.xmlio.entities.runners.Runner;
import org.sing_group.compi.xmlio.entities.runners.Runners;
import org.sing_group.compi.xmlio.runners.DOMRunnersParser;
import org.sing_group.compi.xmlio.runners.RunnersParser;

public class RunnersManager {

  private File runnersXML;
  private Map<String, ProcessCreator> processCreators = new HashMap<>();
  private ProcessCreator defaultProcessCreator;
  private VariableResolver resolver;
  private Runners runners;
  private Pipeline pipeline;

  public RunnersManager(VariableResolver resolver) {
    this.resolver = resolver;
  }

  public RunnersManager(File runnersXML, Pipeline pipeline, VariableResolver resolver) throws IllegalArgumentException, IOException {
    this(resolver);
    this.runnersXML = runnersXML;
    this.pipeline = pipeline;
    this.runners = createRunnersParser().parseXML(this.runnersXML);

    createProcessCreators();
  }

  private void createProcessCreators() {
    this.processCreators.clear();
    this.defaultProcessCreator = null;
    if (runners != null) {
      Set<String> availableTaskIds = pipeline.getTasks().stream().map(Task::getId).collect(toSet());
      for (Runner runner : runners.getRunners()) {
        if (runner.getTasks() == null || runner.getTasks().trim().equals("")) {
          defaultProcessCreator = new CustomRunnerProcessCreator(runner, resolver);
        } else {
          final String[] taskIds = runner.getTasks().split(",");
          for (String taskId : taskIds) {
            taskId = taskId.trim();
            if (processCreators.containsKey(taskId)) {
              throw new IllegalArgumentException("There are more than one runner for task id: " + taskId);
            }
            if (!availableTaskIds.contains(taskId)) {
              throw new IllegalArgumentException("There is no such task id: " + taskId);
            }
            processCreators.put(taskId, new CustomRunnerProcessCreator(runner, resolver));
            availableTaskIds.remove(taskId);
          }
        }
      }
    }
  }

  public ProcessCreator getProcessCreatorForTask(String taskId) {
    if (this.processCreators.containsKey(taskId)) {
      return this.processCreators.get(taskId);
    }
    if (this.defaultProcessCreator != null) {
      return this.defaultProcessCreator;
    }
    return new DefaultProcessCreator(this.resolver);
  }

  protected RunnersParser createRunnersParser() {
    return new DOMRunnersParser();
  }

  public void setResolver(VariableResolver resolver) {
    this.resolver = resolver;
    this.createProcessCreators();
  }
}
