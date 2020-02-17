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
package org.sing_group.compi.core;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sing_group.compi.core.pipeline.Pipeline;
import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * An object containing parameters for a pipeline run. This object is intended
 * for instantiating {@link CompiApp}
 * 
 * To create objects of this class, a builder is provided if you call
 * {@link CompiRunConfiguration#forPipeline(Pipeline)}
 * 
 * @author Daniel Glez-Peña
 * 
 * @see CompiApp
 *
 */
public class CompiRunConfiguration {
  private Pipeline pipeline;
  private File pipelineFile;
  private int maxTasks = 6;
  private VariableResolver resolver;
  private File paramsFile;
  private File runnersFile;
  private String singleTask;
  private List<String> fromTasks;
  private List<String> afterTasks;
  private String untilTask;
  private String beforeTask;
  private File logsDir;
  private boolean overwriteLogs = false;
  private List<String> logOnlyTasks;
  private List<String> doNotLogTasks;
  private boolean showStdOuts = false;

  public static final String CONFIGURATION_VARIABLES_PREFIX = "COMPI_";

  public Map<String, String> asMap() {
    final Map<String, String> asMap = new LinkedHashMap<>();
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "PIPELINE_FILE",
      (this.getPipelineFile() == null) ? "" : this.getPipelineFile().toString()
    );
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "PARAMS_FILE",
      (this.getParamsFile() == null) ? "" : this.getParamsFile().toString()
    );
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "RUNNERS_FILE",
      (this.getRunnersFile() == null) ? "" : this.getRunnersFile().toString()
    );

    asMap.put(CONFIGURATION_VARIABLES_PREFIX + "MAX_TASKS", "" + this.getMaxTasks());

    asMap
      .put(CONFIGURATION_VARIABLES_PREFIX + "BEFORE_TASK", (this.getBeforeTask() == null) ? "" : this.getBeforeTask());
    asMap.put(CONFIGURATION_VARIABLES_PREFIX + "UNTIL_TASK", (this.getUntilTask() == null) ? "" : this.getUntilTask());
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "AFTER_TASKS", (this.getAfterTasks() == null) ? ""
        : this.getAfterTasks().stream().map(Object::toString).collect(Collectors.joining(",")).toString()
    );
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "FROM_TASKS", (this.getFromTasks() == null) ? ""
        : this.getFromTasks().stream().map(Object::toString).collect(Collectors.joining(",")).toString()
    );
    asMap
      .put(CONFIGURATION_VARIABLES_PREFIX + "SINGLE_TASK", (this.getSingleTask() == null) ? "" : this.getSingleTask());

    asMap.put(CONFIGURATION_VARIABLES_PREFIX + "SHOW_STD_OUTS", "" + this.isShowStdOuts());
    asMap.put(
      CONFIGURATION_VARIABLES_PREFIX + "LOGS_DIR", (this.getLogsDir() == null) ? "" : this.getLogsDir().toString()
    );
    asMap.put(CONFIGURATION_VARIABLES_PREFIX + "OVERWRITE_LOGS", "" + this.isOverwriteLogs());
    asMap
      .put(
        CONFIGURATION_VARIABLES_PREFIX + "DO_NOT_LOG_TASKS", (this.getDoNotLogTasks() == null) ? ""
          : this.getDoNotLogTasks().stream().map(Object::toString).collect(Collectors.joining(",")).toString()
      );
    asMap
      .put(
        CONFIGURATION_VARIABLES_PREFIX + "LOG_ONLY_TASKS", (this.getLogOnlyTasks() == null) ? ""
          : this.getLogOnlyTasks().stream().map(Object::toString).collect(Collectors.joining(",")).toString()
      );

    return asMap;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public File getPipelineFile() {
    return pipelineFile;
  }

  public void setPipelineFile(File pipelineFile) {
    this.pipelineFile = pipelineFile;
  }

  public int getMaxTasks() {
    return maxTasks;
  }

  public void setMaxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
  }

  public VariableResolver getResolver() {
    return resolver;
  }

  public void setResolver(VariableResolver resolver) {
    this.resolver = resolver;
  }

  public File getParamsFile() {
    return paramsFile;
  }

  public void setParamsFile(File paramsFile) {
    this.paramsFile = paramsFile;
  }

  public File getRunnersFile() {
    return runnersFile;
  }

  public void setRunnersFile(File runnersFile) {
    this.runnersFile = runnersFile;
  }

  public String getSingleTask() {
    return singleTask;
  }

  public void setSingleTask(String singleTask) {
    this.singleTask = singleTask;
  }

  public List<String> getFromTasks() {
    return fromTasks;
  }

  public void setFromTasks(List<String> fromTasks) {
    this.fromTasks = fromTasks;
  }

  public List<String> getAfterTasks() {
    return afterTasks;
  }

  public void setAfterTasks(List<String> afterTasks) {
    this.afterTasks = afterTasks;
  }

  public String getUntilTask() {
    return untilTask;
  }

  public void setUntilTask(String untilTask) {
    this.untilTask = untilTask;
  }

  public String getBeforeTask() {
    return beforeTask;
  }

  public void setBeforeTask(String beforeTask) {
    this.beforeTask = beforeTask;
  }

  public File getLogsDir() {
    return logsDir;
  }

  public void setLogsDir(File logsDir) {
    this.logsDir = logsDir;
  }

  public List<String> getLogOnlyTasks() {
    return logOnlyTasks;
  }

  public void setLogOnlyTasks(List<String> logOnlyTasks) {
    this.logOnlyTasks = logOnlyTasks;
  }

  public List<String> getDoNotLogTasks() {
    return doNotLogTasks;
  }

  public void setDoNotLogTasks(List<String> doNotLogTasks) {
    this.doNotLogTasks = doNotLogTasks;
  }

  public boolean isOverwriteLogs() {
    return overwriteLogs;
  }

  public void setOverwriteLogs(boolean overwriteLogs) {
    this.overwriteLogs = overwriteLogs;
  }

  public void setShowStdOuts(boolean showStdOuts) {
    this.showStdOuts = showStdOuts;
  }

  public boolean isShowStdOuts() {
    return showStdOuts;
  }

  /**
   * A builder for {@link CompiRunConfiguration} objects
   * 
   * @author Daniel Glez-Peña
   *
   */
  public static class Builder {
    private CompiRunConfiguration config = new CompiRunConfiguration();

    private Builder forPipeline(Pipeline p) {
      this.config.setPipeline(p);
      return this;
    }

    public Builder whichResolvesVariablesWith(VariableResolver resolver) {
      this.config.setResolver(resolver);
      return this;
    }

    public Builder whichRunsTasksUsingCustomRunners(File runnersFile) {
      this.config.setRunnersFile(runnersFile);
      return this;
    }

    public Builder whichRunsAMaximumOf(int maxTasks) {
      this.config.setMaxTasks(maxTasks);
      return this;
    }

    public Builder whichResolvesVariablesFromFile(File f) {
      this.config.setParamsFile(f);
      return this;
    }

    public Builder whichRunsTheSingleTask(String singleTask) {
      this.config.setSingleTask(singleTask);
      return this;
    }

    public Builder whichStartsFromTask(String from) {
      this.config.setFromTasks(Arrays.asList(from));
      return this;
    }

    public Builder whichStartsFromTasks(List<String> from) {
      this.config.setFromTasks(from);
      return this;
    }

    public Builder whichRunsTasksAfterTask(String after) {
      this.config.setAfterTasks(Arrays.asList(after));
      return this;
    }

    public Builder whichRunsTasksAfterTasks(List<String> after) {
      this.config.setAfterTasks(after);
      return this;
    }

    public Builder whichRunsUntilTask(String until) {
      this.config.setUntilTask(until);
      return this;
    }

    public Builder whichRunsTasksBeforeTask(String before) {
      this.config.setBeforeTask(before);
      return this;
    }

    public Builder whichLogsOutputsToDir(File logDir) {
      this.config.logsDir = logDir;
      return this;
    }

    public Builder whichOnlyLogsTasks(List<String> logOnlyTasks) {
      this.config.logOnlyTasks = logOnlyTasks;
      return this;
    }

    public Builder whichDoesNotLogTasks(List<String> doNotLogTasks) {
      this.config.doNotLogTasks = doNotLogTasks;
      return this;
    }

    public Builder whichOverwriteLogs() {
      this.config.overwriteLogs = true;
      return this;
    }

    public Builder whichShowsStdOuts() {
      this.config.showStdOuts = true;
      return this;
    }

    public CompiRunConfiguration build() {
      return config;
    }
  }

  public static Builder forPipeline(Pipeline pipeline) {
    Builder builder = new Builder();
    builder.forPipeline(pipeline);
    return builder;
  }
}
