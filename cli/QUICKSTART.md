# Compi `cli` quickstart
The `cli` module provides a tool that allows you to run pipelines in your host machine. With this utility you can launch your pipeline entirely or partially as well as other functions related to pipelines. 

The `example-files` folder contains a simple pipeline XML file to test the compi `cli` commands. See the main [README.md](README.md) to see how to build this module.

## Table of Contents

   * [1. Validating a pipeline](#1-validating-a-pipeline)
   * [2. Executing the pipeline using a XML parameters file](#2-executing-the-pipeline-using-a-xml-parameters-file)
   * [3. Executing a single pipeline task](#3-executing-a-single-pipeline-task)
   * [4. Executing the pipeline using command-line pipeline parameters](#4-executing-the-pipeline-using-command-line-pipeline-parameters)
   * [5. Export the pipeline graph as a image](#5-export-the-pipeline-graph-as-a-image)
   * [6. Executing the pipeline using a custom task runner](#6-executing-the-pipeline-using-a-custom-task-runner)
   * [7. Run the pipeline until a specific task](#7-run-the-pipeline-until-a-specific-task)
   * [8. Run all the dependencies of a specific task](#8-run-all-the-dependencies-of-a-specific-task)
   * [9. Start the pipeline execution at a specific task](#9-start-the-pipeline-execution-at-a-specific-task)
   * [10. Start the pipeline execution after a specific task](#10-start-the-pipeline-execution-after-a-specific-task)
   * [11. Starting the pipeline execution using both after and <code>from</code>](#11-starting-the-pipeline-execution-using-both-after-and-from)
   * [12. Save tasks outputs in log files](#12-save-tasks-outputs-in-log-files)

## 1. Validating a pipeline
Run the following command to validate the `example-files/pipeline.xml` file:
```
target/dist/compi validate -p example-files/pipeline.xml
```
<details><summary>Command output</summary>
```
[2018-08-17 11:26:24] [INFO   ] Validating pipeline file: example-files/pipeline.xml 
[2018-08-17 11:26:25] [INFO   ] Pipeline file is OK. 
```
</details>

## 2. Executing the pipeline using a XML parameters file
Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`):
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml
```
<details><summary>Command output</summary>
```
[2018-10-09 23:13:45] [INFO] Compi running with:  
[2018-10-09 23:13:46] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:13:46] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:13:46] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:13:46] [INFO] > Started loop task task-1 
[2018-10-09 23:13:46] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:13:46] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:13:46] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:13:46] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:13:46] [INFO] > Started loop task task-10 
[2018-10-09 23:13:46] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:13:47] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:13:47] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:13:49] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:13:49] [INFO] < Finished loop task task-10 
[2018-10-09 23:13:49] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:13:53] [INFO] < Finished loop task task-1 
[2018-10-09 23:13:53] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:13:53] [INFO] > Started task task-2 
[2018-10-09 23:13:53] [INFO] > Started task task-3 
[2018-10-09 23:13:55] [INFO] < Finished task task-3 
[2018-10-09 23:13:55] [INFO] < Finished task task-2 
[2018-10-09 23:13:55] [INFO] > Started task task-6 
[2018-10-09 23:13:55] [INFO] > Started task task-4 
[2018-10-09 23:13:55] [INFO] > Started task task-5 
[2018-10-09 23:13:57] [INFO] < Finished task task-5 
[2018-10-09 23:13:57] [INFO] < Finished task task-4 
[2018-10-09 23:13:57] [INFO] > Started loop task task-7 
[2018-10-09 23:13:57] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:13:58] [INFO] < Finished task task-6 
[2018-10-09 23:13:59] [INFO] < Finished loop task task-7 
[2018-10-09 23:13:59] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:13:59] [INFO] > Started loop task task-8 
[2018-10-09 23:13:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:13:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:13:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:14:00] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:14:00] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:14:04] [INFO] < Finished loop task task-8 
[2018-10-09 23:14:04] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:14:04] [INFO] > Started task task-9 
[2018-10-09 23:14:06] [INFO] < Finished task task-9
```
</details>

## 3. Executing a single pipeline task
Run the following command to execute a single task of the `example-files/pipeline.xml` file, specified with the `--single-task/-st` parameter:
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -st task-10
```

<details><summary>Command output</summary>
```
[2018-10-09 23:14:40] [INFO] Compi running with:  
[2018-10-09 23:14:40] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:14:40] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:14:40] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:14:40] [INFO] Running single task - task-10 
[2018-10-09 23:14:40] [INFO] > Started loop task task-10 
[2018-10-09 23:14:40] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:14:44] [INFO] < Finished loop task task-10 
[2018-10-09 23:14:44] [INFO] << Finished loop iteration of task task-10 
```
</details>

## 4. Executing the pipeline using command-line pipeline parameters
Pipeline parameters can be also indicated in the command line, separated by a `--` after the `compi run` parameters. 

Run the following command to execute the `example-files/pipeline.xml` file providing the required parameters trough the command-line:
```
target/dist/compi run -p example-files/pipeline.xml -- --path example-files/execute/execute.sh --name command-line-name --seconds 1
```

<details><summary>Command output</summary>
```
[2018-10-09 23:14:57] [INFO] Compi running with:  
[2018-10-09 23:14:57] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:14:57] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:14:57] [INFO] > Started loop task task-1 
[2018-10-09 23:14:57] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:14:57] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:14:57] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:14:57] [INFO] > Started loop task task-10 
[2018-10-09 23:14:57] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:14:57] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:14:58] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:14:58] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:00] [INFO] < Finished loop task task-10 
[2018-10-09 23:15:00] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:15:00] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:04] [INFO] < Finished loop task task-1 
[2018-10-09 23:15:04] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:04] [INFO] > Started task task-2 
[2018-10-09 23:15:04] [INFO] > Started task task-3 
[2018-10-09 23:15:06] [INFO] < Finished task task-2 
[2018-10-09 23:15:06] [INFO] < Finished task task-3 
[2018-10-09 23:15:06] [INFO] > Started task task-5 
[2018-10-09 23:15:06] [INFO] > Started task task-4 
[2018-10-09 23:15:06] [INFO] > Started task task-6 
[2018-10-09 23:15:07] [INFO] < Finished task task-4 
[2018-10-09 23:15:08] [INFO] < Finished task task-5 
[2018-10-09 23:15:08] [INFO] > Started loop task task-7 
[2018-10-09 23:15:08] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:15:09] [INFO] < Finished task task-6 
[2018-10-09 23:15:09] [INFO] < Finished loop task task-7 
[2018-10-09 23:15:09] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:15:09] [INFO] > Started loop task task-8 
[2018-10-09 23:15:09] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:15:09] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:15:09] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:15:10] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:15:10] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:15:14] [INFO] < Finished loop task task-8 
[2018-10-09 23:15:14] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:15:14] [INFO] > Started task task-9 
[2018-10-09 23:15:16] [INFO] < Finished task task-9
```
</details>

## 5. Export the pipeline graph as an image
Run the following command to export the graph defined by the `example-files/pipeline.xml` pipeline as an image.
```
target/dist/compi export-graph -p example-files/pipeline.xml -o pipeline.png -f png
```

<details><summary>Command output</summary>
```
[2018-08-17 11:48:33] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:48:33] [INFO] Export graph to file - pipeline.png 
[2018-08-17 11:48:33] [INFO] Graph format - png 
[2018-08-17 11:48:33] [INFO] Graph orientation - vertical 
[2018-08-17 11:48:33] [INFO] Graph font size - 10
```
</details>

If you want to draw also the task parameters, try options `--draw-task-params` or `--draw-pipeline-params`.

## 6. Executing the pipeline using a custom task runner
It is possible to run pipeline tasks using custom runners, which must be defined in XML passed with the `-r` or `--runners-config` parameter. This mode is meant to allow users to run tasks using ways different than the default `/bin/sh -c` used by Compi, such as running tasks in Docker images or using work managers (e.g. Slurm, qsub).

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) with the custom runner defined in the `example-files/pipeline-runner.xml` file. This runner simply writes a log in `/tmp/runner-output.txt` and runs each task using `/bin/sh -c`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -r example-files/pipeline-runner.xml
```

<details><summary>Command output</summary>
```
[2018-10-09 23:15:54] [INFO] Compi running with:  
[2018-10-09 23:15:54] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:15:54] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:15:54] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:15:54] [INFO] Runners file - example-files/pipeline-runner.xml 
[2018-10-09 23:15:54] [INFO] > Started loop task task-1 
[2018-10-09 23:15:54] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:15:54] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:15:54] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:15:54] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:15:54] [INFO] > Started loop task task-10 
[2018-10-09 23:15:54] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:15:55] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:55] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:57] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:15:57] [INFO] < Finished loop task task-10 
[2018-10-09 23:15:57] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:16:01] [INFO] < Finished loop task task-1 
[2018-10-09 23:16:01] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:16:01] [INFO] > Started task task-2 
[2018-10-09 23:16:01] [INFO] > Started task task-3 
[2018-10-09 23:16:03] [INFO] < Finished task task-2 
[2018-10-09 23:16:03] [INFO] < Finished task task-3 
[2018-10-09 23:16:03] [INFO] > Started task task-5 
[2018-10-09 23:16:03] [INFO] > Started task task-4 
[2018-10-09 23:16:03] [INFO] > Started task task-6 
[2018-10-09 23:16:05] [INFO] < Finished task task-5 
[2018-10-09 23:16:05] [INFO] < Finished task task-4 
[2018-10-09 23:16:05] [INFO] > Started loop task task-7 
[2018-10-09 23:16:05] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:16:06] [INFO] < Finished task task-6 
[2018-10-09 23:16:07] [INFO] < Finished loop task task-7 
[2018-10-09 23:16:07] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:16:07] [INFO] > Started loop task task-8 
[2018-10-09 23:16:07] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:16:07] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:16:07] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:16:08] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:16:08] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:16:12] [INFO] < Finished loop task task-8 
[2018-10-09 23:16:12] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:16:12] [INFO] > Started task task-9 
[2018-10-09 23:16:14] [INFO] < Finished task task-9
```
</details>

<details><summary>Contents of `/tmp/runner-output.txt`</summary>
```
[task-1] code: ${path} ${name} ${nonParamSeconds}
[task-1] code: ${path} ${name} ${nonParamSeconds}
[task-1] code: ${path} ${name} ${nonParamSeconds}
[task-1] code: ${path} ${name} ${nonParamSeconds}
[task-10] code: ${path} p10 3
[task-2] code: example-files/execute/execute.sh p2 2
[task-3] code: example-files/execute/execute.sh p3 2
[task-5] code: example-files/execute/execute.sh p5 2
[task-4] code: ${path} p4 ${seconds}
[task-6] code: example-files/execute/execute.sh p6 3
[task-7] code: ${path} p7 ${seconds}
[task-8] code: ${path} p8 ${nonParamSeconds}
[task-8] code: ${path} p8 ${nonParamSeconds}
[task-8] code: ${path} p8 ${nonParamSeconds}
[task-9] code: example-files/execute/execute.sh p9 2
```
</details>

## 7. Run the pipeline until a specific task
It is possible to run a pipeline until a specific task, including all its dependencies. 

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) until task `task-7`. This command will run `task-1`, `task-2`, `task-4`, `task-5` and `task-7`.

```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -ut task-7
```

<details><summary>Command output</summary>
```
[2018-10-09 23:17:11] [INFO] Compi running with:  
[2018-10-09 23:17:11] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:17:11] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:17:11] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:17:11] [INFO] Running until task - task-7 
[2018-10-09 23:17:11] [INFO] > Started loop task task-1 
[2018-10-09 23:17:11] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:11] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:11] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:11] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:12] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:12] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:14] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:18] [INFO] < Finished loop task task-1 
[2018-10-09 23:17:18] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:18] [INFO] > Started task task-2 
[2018-10-09 23:17:20] [INFO] < Finished task task-2 
[2018-10-09 23:17:20] [INFO] > Started task task-4 
[2018-10-09 23:17:20] [INFO] > Started task task-5 
[2018-10-09 23:17:22] [INFO] < Finished task task-4 
[2018-10-09 23:17:22] [INFO] < Finished task task-5 
[2018-10-09 23:17:22] [INFO] > Started loop task task-7 
[2018-10-09 23:17:22] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:17:24] [INFO] < Finished loop task task-7 
[2018-10-09 23:17:24] [INFO] << Finished loop iteration of task task-7
```
</details>

## 8. Run all the dependencies of a specific task
It is possible to run all the dependencies a specific task.

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) until task `task-7`. This command will run `task-1`, `task-2`, `task-4` and `task-5`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -bt task-7
```

<details><summary>Command output</summary>
```
[2018-10-09 23:17:41] [INFO] Compi running with:  
[2018-10-09 23:17:41] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:17:41] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:17:41] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:17:41] [INFO] Running tasks before task - task-7 
[2018-10-09 23:17:41] [INFO] > Started loop task task-1 
[2018-10-09 23:17:41] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:41] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:41] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:41] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:17:42] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:42] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:44] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:48] [INFO] < Finished loop task task-1 
[2018-10-09 23:17:48] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:17:48] [INFO] > Started task task-2 
[2018-10-09 23:17:50] [INFO] < Finished task task-2 
[2018-10-09 23:17:50] [INFO] > Started task task-4 
[2018-10-09 23:17:50] [INFO] > Started task task-5 
[2018-10-09 23:17:52] [INFO] < Finished task task-4 
[2018-10-09 23:17:52] [INFO] < Finished task task-5 
```
</details>

## 9. Start the pipeline execution at a specific task
It is possible start the pipeline execution at a specific task (e.g. in order to resume the execution after an error or in combination with `before`, `until` and `single-task` executions). 

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) starting at task `task-7`. This command will run all tasks that do not depend on `task-7`, that is: `task-7`, `task-3`, `task-10`, `task-6`, `task-8`, and `task-9`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -f task-7
```

<details><summary>Command output</summary>
```
[2018-10-09 23:18:15] [INFO] Compi running with:  
[2018-10-09 23:18:15] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:18:15] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:18:15] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:18:15] [INFO] Running from task(s) - task-7 
[2018-10-09 23:18:15] [INFO] > Started loop task task-10 
[2018-10-09 23:18:15] [INFO] > Started task task-3 
[2018-10-09 23:18:15] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:18:15] [INFO] > Started loop task task-7 
[2018-10-09 23:18:15] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:18:17] [INFO] < Finished task task-3 
[2018-10-09 23:18:17] [INFO] > Started task task-6 
[2018-10-09 23:18:17] [INFO] < Finished loop task task-7 
[2018-10-09 23:18:17] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:18:18] [INFO] < Finished loop task task-10 
[2018-10-09 23:18:18] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:18:20] [INFO] < Finished task task-6 
[2018-10-09 23:18:20] [INFO] > Started loop task task-8 
[2018-10-09 23:18:20] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:18:20] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:18:20] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:18:21] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:18:21] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:18:25] [INFO] < Finished loop task task-8 
[2018-10-09 23:18:25] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:18:25] [INFO] > Started task task-9 
[2018-10-09 23:18:27] [INFO] < Finished task task-9
```
</details>

## 10. Start the pipeline execution after a specific task
It is possible start the pipeline execution after a specific task (e.g. in order to resume the execution after an error or in combination with `before`, `until` and `single-task` executions). This is similar to the previous example, with the difference that the task specified with `after` is not executed.

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) starting after task `task-7`. This command will run all tasks that do not depend on `task-7`, that is: `task-3`, `task-10`, `task-6`, `task-8`, and `task-9`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -a task-7
```

<details><summary>Command output</summary>
```
[2018-10-09 23:18:54] [INFO] Compi running with:  
[2018-10-09 23:18:54] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:18:54] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:18:54] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:18:54] [INFO] Running after task(s) - task-7 
[2018-10-09 23:18:54] [INFO] > Started loop task task-10 
[2018-10-09 23:18:54] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:18:54] [INFO] > Started task task-3 
[2018-10-09 23:18:56] [INFO] < Finished task task-3 
[2018-10-09 23:18:56] [INFO] > Started task task-6 
[2018-10-09 23:18:57] [INFO] < Finished loop task task-10 
[2018-10-09 23:18:57] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:18:59] [INFO] < Finished task task-6 
[2018-10-09 23:18:59] [INFO] > Started loop task task-8 
[2018-10-09 23:18:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:18:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:18:59] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:19:00] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:00] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:04] [INFO] < Finished loop task task-8 
[2018-10-09 23:19:04] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:04] [INFO] > Started task task-9 
[2018-10-09 23:19:06] [INFO] < Finished task task-9
```
</details>

## 11. Starting the pipeline execution using both `after` and `from`
It is possible to specify multiple `after` and `from` tasks and even specify both of them in the same execution.

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) starting at task `task-7` and also starting after `task-3`. This command will run all tasks that do not depend on `task-7`, including it, and that do not depend on `task-3`, that is: `task-10`, `task-7`, `task-6`, `task-8`, and `task-9`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -f task-7 -a task-3
```

<details><summary>Command output</summary>
```
[2018-10-09 23:19:26] [INFO] Compi running with:  
[2018-10-09 23:19:26] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:19:26] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:19:26] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:19:26] [INFO] Running from task(s) - task-7 
[2018-10-09 23:19:26] [INFO] Running after task(s) - task-3 
[2018-10-09 23:19:27] [INFO] > Started task task-6 
[2018-10-09 23:19:27] [INFO] > Started loop task task-10 
[2018-10-09 23:19:27] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:19:27] [INFO] > Started loop task task-7 
[2018-10-09 23:19:27] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:19:29] [INFO] < Finished loop task task-7 
[2018-10-09 23:19:29] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:19:30] [INFO] < Finished task task-6 
[2018-10-09 23:19:30] [INFO] < Finished loop task task-10 
[2018-10-09 23:19:30] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:19:30] [INFO] > Started loop task task-8 
[2018-10-09 23:19:30] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:19:30] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:19:30] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:19:31] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:31] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:35] [INFO] < Finished loop task task-8 
[2018-10-09 23:19:35] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:19:35] [INFO] > Started task task-9 
[2018-10-09 23:19:37] [INFO] < Finished task task-9
```

## 12. Save tasks outputs in log files
By default, the standard and error outputs of each task executions are not saved. It is possible to save these outputs in log files by using the option `--logs/-l </path/to/logs/directory>`. Since this option tells compi to create logs for all tasks, it is possible to select specific tasks to log with `--log-only-task` or `--no-log-task`.

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) generating logs in `/tmp`.

```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -l tmp
```

<details><summary>Command output</summary>
```
[2018-10-09 23:24:25] [INFO] Compi running with:  
[2018-10-09 23:24:25] [INFO] Pipeline file - example-files/pipeline.xml 
[2018-10-09 23:24:25] [INFO] Max number of parallel tasks - 6 
[2018-10-09 23:24:25] [INFO] Params file - example-files/params.xml 
[2018-10-09 23:24:25] [INFO] Logging task's output to dir - /tmp 
[2018-10-09 23:24:25] [INFO] > Started loop task task-1 
[2018-10-09 23:24:25] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:24:25] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:24:25] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:24:25] [INFO] >> Started loop iteration of task task-1 
[2018-10-09 23:24:25] [INFO] > Started loop task task-10 
[2018-10-09 23:24:25] [INFO] >> Started loop iteration of task task-10 
[2018-10-09 23:24:26] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:24:26] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:24:28] [INFO] < Finished loop task task-10 
[2018-10-09 23:24:28] [INFO] << Finished loop iteration of task task-10 
[2018-10-09 23:24:28] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:24:32] [INFO] < Finished loop task task-1 
[2018-10-09 23:24:32] [INFO] << Finished loop iteration of task task-1 
[2018-10-09 23:24:32] [INFO] > Started task task-2 
[2018-10-09 23:24:32] [INFO] > Started task task-3 
[2018-10-09 23:24:34] [INFO] < Finished task task-2 
[2018-10-09 23:24:34] [INFO] < Finished task task-3 
[2018-10-09 23:24:34] [INFO] > Started task task-5 
[2018-10-09 23:24:34] [INFO] > Started task task-4 
[2018-10-09 23:24:34] [INFO] > Started task task-6 
[2018-10-09 23:24:36] [INFO] < Finished task task-5 
[2018-10-09 23:24:36] [INFO] < Finished task task-4 
[2018-10-09 23:24:36] [INFO] > Started loop task task-7 
[2018-10-09 23:24:36] [INFO] >> Started loop iteration of task task-7 
[2018-10-09 23:24:37] [INFO] < Finished task task-6 
[2018-10-09 23:24:38] [INFO] < Finished loop task task-7 
[2018-10-09 23:24:38] [INFO] << Finished loop iteration of task task-7 
[2018-10-09 23:24:38] [INFO] > Started loop task task-8 
[2018-10-09 23:24:38] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:24:38] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:24:38] [INFO] >> Started loop iteration of task task-8 
[2018-10-09 23:24:39] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:24:39] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:24:43] [INFO] < Finished loop task task-8 
[2018-10-09 23:24:43] [INFO] << Finished loop iteration of task task-8 
[2018-10-09 23:24:43] [INFO] > Started task task-9 
[2018-10-09 23:24:45] [INFO] < Finished task task-9
```
