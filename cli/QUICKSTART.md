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
[2018-08-17 11:27:57] [INFO   ] Compi running with:  
[2018-08-17 11:27:57] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:27:57] [INFO   ] Max number of parallel tasks - 6 
[2018-08-17 11:27:57] [INFO   ] Params file - example-files/params.xml 
[2018-08-17 11:27:57] [INFO   ] > Started loop task task-1 (command: example-files/execute/execute.sh p1 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:27:57] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:27:57] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:27:57] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:27:57] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:27:57] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:27:57] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:27:58] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-08-17 11:27:58] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-08-17 11:28:00] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) 
[2018-08-17 11:28:00] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-08-17 11:28:00] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-08-17 11:28:04] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-08-17 11:28:04] [INFO   ] < Finished loop task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-08-17 11:28:04] [INFO   ] > Started task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-08-17 11:28:04] [INFO   ] > Started task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-08-17 11:28:06] [INFO   ] < Finished task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-08-17 11:28:06] [INFO   ] > Started task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-08-17 11:28:06] [INFO   ] > Started task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-08-17 11:28:06] [INFO   ] < Finished task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-08-17 11:28:06] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-08-17 11:28:08] [INFO   ] < Finished task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-08-17 11:28:08] [INFO   ] < Finished task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-08-17 11:28:08] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-08-17 11:28:08] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-08-17 11:28:09] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-08-17 11:28:10] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-08-17 11:28:10] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-08-17 11:28:10] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:28:10] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:28:10] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:28:10] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:28:11] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-08-17 11:28:11] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-08-17 11:28:15] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-08-17 11:28:15] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-08-17 11:28:15] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-08-17 11:28:17] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2) 
```
</details>

## 3. Executing a single pipeline task
Run the following command to execute a single task of the `example-files/pipeline.xml` file, specified with the `--single-task/-st` parameter:
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -st task-10
```

<details><summary>Command output</summary>
```
[2018-08-17 11:36:25] [INFO   ] Compi running with:  
[2018-08-17 11:36:25] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:36:25] [INFO   ] Max number of parallel tasks - 6 
[2018-08-17 11:36:25] [INFO   ] Params file - example-files/params.xml 
[2018-08-17 11:36:25] [INFO   ] Running single task - task-10
[2018-08-17 11:36:25] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:36:25] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:36:28] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-08-17 11:36:28] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
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
[2018-08-17 11:40:07] [INFO   ] Compi running with:  
[2018-08-17 11:40:07] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:40:07] [INFO   ] Max number of parallel tasks - 6 
[2018-08-17 11:40:07] [INFO   ] > Started loop task task-1 (command: example-files/execute/execute.sh command-line-name 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:40:07] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:40:07] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:40:07] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-08-17 11:40:07] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:40:07] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:40:07] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-08-17 11:40:08] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 1) 
[2018-08-17 11:40:08] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 1) 
[2018-08-17 11:40:10] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-08-17 11:40:10] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-08-17 11:40:10] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 3) 
[2018-08-17 11:40:14] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh command-line-name 7) 
[2018-08-17 11:40:14] [INFO   ] < Finished loop task task-1 (command: example-files/execute/execute.sh command-line-name 7) 
[2018-08-17 11:40:14] [INFO   ] > Started task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-08-17 11:40:14] [INFO   ] > Started task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-08-17 11:40:16] [INFO   ] < Finished task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-08-17 11:40:16] [INFO   ] < Finished task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-08-17 11:40:16] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-08-17 11:40:16] [INFO   ] > Started task task-4 (command: example-files/execute/execute.sh p4 1) 
[2018-08-17 11:40:16] [INFO   ] > Started task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-08-17 11:40:17] [INFO   ] < Finished task task-4 (command: example-files/execute/execute.sh p4 1) 
[2018-08-17 11:40:18] [INFO   ] < Finished task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-08-17 11:40:18] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 1) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-08-17 11:40:18] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 1) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-08-17 11:40:19] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-08-17 11:40:19] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 1) 
[2018-08-17 11:40:19] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 1) 
[2018-08-17 11:40:19] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:40:19] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:40:19] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:40:19] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-08-17 11:40:20] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-08-17 11:40:20] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-08-17 11:40:24] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-08-17 11:40:24] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-08-17 11:40:24] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-08-17 11:40:26] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2)
```
</details>

## 5. Export the pipeline graph as an image
Run the following command to export the graph defined by the `example-files/pipeline.xml` pipeline as an image.
```
target/dist/compi export-graph -p example-files/pipeline.xml -o pipeline.png -f png
```

<details><summary>Command output</summary>
```
[2018-08-17 11:48:33] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:48:33] [INFO   ] Export graph to file - pipeline.png 
[2018-08-17 11:48:33] [INFO   ] Graph format - png 
[2018-08-17 11:48:33] [INFO   ] Graph orientation - vertical 
[2018-08-17 11:48:33] [INFO   ] Graph font size - 10
```
</details>

## 6. Executing the pipeline using a custom task runner
It is possible to run pipeline tasks using custom runners, which must be defined in XML passed with the `-r` or `--runners-config` parameter. This mode is meant to allow users to run tasks using ways different than the default `/bin/sh -c` used by Compi, such as running tasks in Docker images or using work managers (e.g. Slurm, qsub).

Run the following command to execute the `example-files/pipeline.xml` file using the example parameters file (`example-files/params.xml`) with the custom runner defined in the `example-files/pipeline-runner.xml` file. This runner simply writes a log in `/tmp/runner-output.txt` and runs each task using `/bin/sh -c`.
```
target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml -r example-files/pipeline-runner.xml
```

<details><summary>Command output</summary>
```
[2018-09-04 22:31:31] [INFO   ] Compi running with:  
[2018-09-04 22:31:31] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-04 22:31:31] [INFO   ] Max number of parallel tasks - 6 
[2018-09-04 22:31:31] [INFO   ] Params file - example-files/params.xml 
[2018-09-06 09:25:08] [INFO   ] Runners file - example-files/pipeline-runner.xml
[2018-09-04 22:31:31] [INFO   ] > Started loop task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-04 22:31:31] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-04 22:31:31] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-04 22:31:31] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-04 22:31:31] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-04 22:31:31] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-04 22:31:31] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-04 22:31:32] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-04 22:31:32] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-04 22:31:34] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) 
[2018-09-04 22:31:34] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-04 22:31:34] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-04 22:31:38] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-04 22:31:38] [INFO   ] < Finished loop task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-04 22:31:38] [INFO   ] > Started task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-04 22:31:38] [INFO   ] > Started task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-04 22:31:40] [INFO   ] < Finished task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-04 22:31:40] [INFO   ] > Started task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-09-04 22:31:40] [INFO   ] < Finished task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-04 22:31:40] [INFO   ] > Started task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-04 22:31:40] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-04 22:31:42] [INFO   ] < Finished task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-09-04 22:31:42] [INFO   ] < Finished task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-04 22:31:42] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-04 22:31:42] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-04 22:31:43] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-04 22:31:44] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-04 22:31:44] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-04 22:31:44] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-04 22:31:44] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-04 22:31:44] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-04 22:31:44] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-04 22:31:45] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-04 22:31:45] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-04 22:31:49] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-04 22:31:49] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-04 22:31:49] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-09-04 22:31:51] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2)
```
</details>

<details><summary>Contents of `/tmp/runner-output.txt`</summary>
```
[t1] code: echo hello > /tmp/t1-result
[t3] iteration-value: 1 my-var: hello code: echo 1 >> /tmp/t3-result
[t3] iteration-value: 2 my-var: hello code: echo 2 >> /tmp/t3-result
[t3] iteration-value: 3 my-var: hello code: echo 3 >> /tmp/t3-result
[t2] code: echo task-2 > /tmp/t2-result
[task-1] code: example-files/execute/execute.sh p1 7
[task-1] code: example-files/execute/execute.sh p1 3
[task-1] code: example-files/execute/execute.sh p1 1
[task-1] code: example-files/execute/execute.sh p1 1
[task-10] code: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3
[task-2] code: example-files/execute/execute.sh p2 2
[task-3] code: example-files/execute/execute.sh p3 2
[task-4] code: example-files/execute/execute.sh p4 2
[task-5] code: example-files/execute/execute.sh p5 2
[task-6] code: example-files/execute/execute.sh p6 3
[task-7] code: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2
[task-8] code: example-files/execute/execute.sh p8 1
[task-8] code: example-files/execute/execute.sh p8 1
[task-8] code: example-files/execute/execute.sh p8 5
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
[2018-09-05 22:23:18] [INFO   ] Compi running with:  
[2018-09-05 22:23:18] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-05 22:23:18] [INFO   ] Max number of parallel tasks - 6 
[2018-09-05 22:23:18] [INFO   ] Params file - example-files/params.xml 
[2018-09-05 22:23:18] [INFO   ] Running until task - task-7
[2018-09-05 22:23:18] [INFO   ] > Started loop task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:23:18] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:23:18] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:23:18] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:23:18] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:23:19] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-05 22:23:19] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-05 22:23:21] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) 
[2018-09-05 22:23:25] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-05 22:23:25] [INFO   ] < Finished loop task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-05 22:23:25] [INFO   ] > Started task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-05 22:23:27] [INFO   ] < Finished task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-05 22:23:27] [INFO   ] > Started task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-09-05 22:23:27] [INFO   ] > Started task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-05 22:23:29] [INFO   ] < Finished task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-05 22:23:29] [INFO   ] < Finished task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-09-05 22:23:29] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-05 22:23:29] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-05 22:23:31] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-05 22:23:31] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
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
[2018-09-05 22:25:58] [INFO   ] Compi running with:  
[2018-09-05 22:25:58] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-05 22:25:58] [INFO   ] Max number of parallel tasks - 6 
[2018-09-05 22:25:58] [INFO   ] Params file - example-files/params.xml 
[2018-09-05 22:25:58] [INFO   ] Running tasks before task - task-7
[2018-09-05 22:25:58] [INFO   ] > Started loop task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:25:58] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:25:58] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:25:58] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:25:58] [INFO   ] >> Started loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) (stdout log: /tmp/task1.txt, stderr log: /tmp/task1.txt) 
[2018-09-05 22:25:59] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-05 22:25:59] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 1) 
[2018-09-05 22:26:01] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 3) 
[2018-09-05 22:26:05] [INFO   ] << Finished loop iteration of task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-05 22:26:05] [INFO   ] < Finished loop task task-1 (command: example-files/execute/execute.sh p1 7) 
[2018-09-05 22:26:05] [INFO   ] > Started task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-05 22:26:07] [INFO   ] < Finished task task-2 (command: example-files/execute/execute.sh p2 2) 
[2018-09-05 22:26:07] [INFO   ] > Started task task-4 (command: example-files/execute/execute.sh p4 2) 
[2018-09-05 22:26:07] [INFO   ] > Started task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-05 22:26:09] [INFO   ] < Finished task task-5 (command: example-files/execute/execute.sh p5 2) 
[2018-09-05 22:26:09] [INFO   ] < Finished task task-4 (command: example-files/execute/execute.sh p4 2) 
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
[2018-09-05 22:29:16] [INFO   ] Compi running with:  
[2018-09-05 22:29:16] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-05 22:29:16] [INFO   ] Max number of parallel tasks - 6 
[2018-09-05 22:29:16] [INFO   ] Params file - example-files/params.xml 
[2018-09-05 22:29:16] [INFO   ] From task - task-7
[2018-09-05 22:29:16] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-05 22:29:16] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-05 22:29:16] [INFO   ] > Started task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-05 22:29:16] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-05 22:29:16] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-05 22:29:18] [INFO   ] < Finished task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-05 22:29:18] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-05 22:29:18] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-05 22:29:18] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-05 22:29:19] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-05 22:29:19] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-05 22:29:21] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-05 22:29:21] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-05 22:29:21] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-05 22:29:21] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-05 22:29:21] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-05 22:29:22] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-05 22:29:22] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-05 22:29:26] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-05 22:29:26] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-05 22:29:26] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-09-05 22:29:28] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2) 
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
[2018-09-07 09:25:48] [INFO   ] Compi running with:  
[2018-09-07 09:25:48] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-07 09:25:48] [INFO   ] Max number of parallel tasks - 6 
[2018-09-07 09:25:48] [INFO   ] Params file - example-files/params.xml 
[2018-09-07 09:25:48] [INFO   ] Running after task(s) - [task-7] 
[2018-09-07 09:25:48] [INFO   ] > Started task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-07 09:25:48] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-07 09:25:48] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-07 09:25:50] [INFO   ] < Finished task task-3 (command: example-files/execute/execute.sh p3 2) 
[2018-09-07 09:25:50] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-07 09:25:51] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-07 09:25:51] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-07 09:25:53] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-07 09:25:53] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 09:25:53] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 09:25:53] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 09:25:53] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 09:25:54] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-07 09:25:54] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-07 09:25:58] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-07 09:25:58] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-07 09:25:58] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-09-07 09:26:00] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2) 
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
[2018-09-07 10:58:40] [INFO   ] Compi running with:  
[2018-09-07 10:58:40] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-09-07 10:58:40] [INFO   ] Max number of parallel tasks - 6 
[2018-09-07 10:58:40] [INFO   ] Params file - example-files/params.xml 
[2018-09-07 10:58:40] [INFO   ] Running from task(s) - task-7 
[2018-09-07 10:58:40] [INFO   ] Running after task(s) - task-3 
[2018-09-07 10:58:40] [INFO   ] > Started loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-07 10:58:40] [INFO   ] >> Started loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) (stdout log: none, stderr log: none) 
[2018-09-07 10:58:40] [INFO   ] > Started task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-07 10:58:40] [INFO   ] > Started loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-07 10:58:40] [INFO   ] >> Started loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) (stdout log: /tmp/task7.txt, stderr log: none) 
[2018-09-07 10:58:42] [INFO   ] << Finished loop iteration of task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-07 10:58:42] [INFO   ] < Finished loop task task-7 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p7 2) 
[2018-09-07 10:58:43] [INFO   ] << Finished loop iteration of task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-07 10:58:43] [INFO   ] < Finished loop task task-10 (command: /home/hlfernandez/Eclipse/workspace-oxygen/compi/cli/example-files/execute/execute.sh p10 3) 
[2018-09-07 10:58:43] [INFO   ] < Finished task task-6 (command: example-files/execute/execute.sh p6 3) 
[2018-09-07 10:58:43] [INFO   ] > Started loop task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 10:58:43] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 10:58:43] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 10:58:43] [INFO   ] >> Started loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) (stdout log: /tmp/task8.txt, stderr log: /tmp/error8.txt) 
[2018-09-07 10:58:44] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-07 10:58:44] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 1) 
[2018-09-07 10:58:48] [INFO   ] << Finished loop iteration of task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-07 10:58:48] [INFO   ] < Finished loop task task-8 (command: example-files/execute/execute.sh p8 5) 
[2018-09-07 10:58:48] [INFO   ] > Started task task-9 (command: example-files/execute/execute.sh p9 2) 
[2018-09-07 10:58:50] [INFO   ] < Finished task task-9 (command: example-files/execute/execute.sh p9 2) 
```
