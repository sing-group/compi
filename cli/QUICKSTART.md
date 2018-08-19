# Compi `cli` quickstart
The `cli` module provides a tool that allows you to run pipelines in your host machine. With this utility you can launch your pipeline entirely or partially as well as other functions related to pipelines. 

The `example-files` folder contains a simple pipeline XML file to test the compi `cli` commands. See the main [README.md](README.md) to see how to build this module.

## 1. Validating a pipeline
Run the following command to validate the `example-files/pipeline.xml` file:
```
target/dist/compi validate -p example-files/pipeline.xml
```
<details><summary>Command output</summary>
```
[2018-08-17 11:26:24] [INFO   ] Validating pipeline file: example-files/pipeline.xml 
[2018-08-17 11:26:25] [WARNING] WARNING_MISSING_PARAM_DESCRIPTION: The parameter "name" has no <param> section for discribing it. 
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
[2018-08-17 11:26:24] [INFO   ] Validating pipeline file: example-files/pipeline.xml 
[2018-08-17 11:26:25] [WARNING] WARNING_MISSING_PARAM_DESCRIPTION: The parameter "name" has no <param> section for discribing it. 
[2018-08-17 11:26:25] [INFO   ] Pipeline file is OK. 
hlfernandez@hlfernandez-mountain:~/Eclipse/workspace-oxygen/compi/cli$ target/dist/compi run -p example-files/pipeline.xml -pa example-files/params.xml
[2018-08-17 11:27:57] [INFO   ] Compi running with:  
[2018-08-17 11:27:57] [INFO   ] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:27:57] [INFO   ] Number of threads - 6 
[2018-08-17 11:27:57] [INFO   ] Params file - example-files/params.xml 
[2018-08-17 11:27:57] [WARNING] WARNING_MISSING_PARAM_DESCRIPTION: The parameter "name" has no <param> section for discribing it. 
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
[2018-08-17 11:36:25] [INFO   ] Number of threads - 6 
[2018-08-17 11:36:25] [INFO   ] Params file - example-files/params.xml 
[2018-08-17 11:36:25] [INFO   ] Running single task - task-10
 
[2018-08-17 11:36:25] [WARNING] WARNING_MISSING_PARAM_DESCRIPTION: The parameter "name" has no <param> section for discribing it. 
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
[2018-08-17 11:40:07] [INFO   ] Number of threads - 6 
[2018-08-17 11:40:07] [WARNING] WARNING_MISSING_PARAM_DESCRIPTION: The parameter "name" has no <param> section for discribing it. 
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

## 5. Export the pipeline graph as a image
Run the following command to export the graph defined by the `example-files/pipeline.xml` pipeline as an image.
```
target/dist/compi export-graph -p example-files/pipeline.xml -o pipeline.png -f png
```
<details><summary>Command output</summary>
```
[2018-08-17 11:48:33] [INFORMACIÓN] Pipeline file - example-files/pipeline.xml 
[2018-08-17 11:48:33] [INFORMACIÓN] Export graph to file - pipeline.png 
[2018-08-17 11:48:33] [INFORMACIÓN] Graph format - png 
[2018-08-17 11:48:33] [INFORMACIÓN] Graph orientation - vertical 
[2018-08-17 11:48:33] [INFORMACIÓN] Graph font size - 10
```
</details>