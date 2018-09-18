package org.sing_group.compi.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sing_group.compi.core.resolver.VariableResolver;

/**
 * An object containing parameters for a pipeline run. This object is intended for instantiating {@link CompiApp}
 * 
 * To create objects of this class, a builder is provided if you call {@link CompiRunConfiguration#forFile(File)}
 * 
 * @author Daniel Glez-Peña
 * 
 * @see CompiApp
 *
 */
public class CompiRunConfiguration {
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

  public File getPipelineFile() {
    return pipelineFile;
  }

  private void setPipelineFile(File pipelineFile) {
    this.pipelineFile = pipelineFile;
  }

  public int getMaxTasks() {
    return maxTasks;
  }

  private void setMaxTasks(int maxTasks) {
    this.maxTasks = maxTasks;
  }

  public VariableResolver getResolver() {
    return resolver;
  }

  private void setResolver(VariableResolver resolver) {
    this.resolver = resolver;
  }

  public File getParamsFile() {
    return paramsFile;
  }
  
  private void setParamsFile(File paramsFile) {
    this.paramsFile = paramsFile;
  }
  
  public File getRunnersFile() {
    return runnersFile;
  }
  
  private void setRunnersFile(File runnersFile) {
    this.runnersFile = runnersFile;
  }
  
  public String getSingleTask() {
    return singleTask;
  }

  private void setSingleTask(String singleTask) {
    this.singleTask = singleTask;
  }

  public List<String> getFromTasks() {
    return fromTasks;
  }

  private void setFromTasks(List<String> fromTasks) {
    this.fromTasks = fromTasks;
  }

  public List<String> getAfterTasks() {
    return afterTasks;
  }

  private void setAfterTasks(List<String> afterTasks) {
    this.afterTasks = afterTasks;
  }

  public String getUntilTask() {
    return untilTask;
  }

  private void setUntilTask(String untilTask) {
    this.untilTask = untilTask;
  }

  public String getBeforeTask() {
    return beforeTask;
  }

  private void setBeforeTask(String beforeTask) {
    this.beforeTask = beforeTask;
  }

  /**
   * A builder for {@link CompiRunConfiguration} objects
   * 
   * @author Daniel Glez-Peña
   *
   */
  public static class Builder {
    private CompiRunConfiguration config = new CompiRunConfiguration();

    private Builder forFile(File f) {
      this.config.setPipelineFile(f);
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
    
    public CompiRunConfiguration build() {
      return config;
    }
  }
  
  public static Builder forFile(File file) {
    Builder builder = new Builder();
    builder.forFile(file);
    return builder;
  }
}
