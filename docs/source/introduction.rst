Introduction
************

.. figure:: images/compi.png
   :align: center

What is Compi?
================

Compi is an application development framework for portable computational pipelines. A software pipeline is a chain of processing elements so that the output of each element is the input of the next.

There are many fields where computational pipelines constitute the main architecture of applications, such as big data analysis or bioinformatics.

Many pipelines combine third party tools along with custom made processes, conforming the final pipeline. Compi is the framework helping to create the final, portable application.

Main features
=============

The main features of Compi are:

- **Language agnostic**: Compi pipelines are defined in XML, where each task is run in an external program written in any programming language. If your program is a mere combination of existing tools, it is not necessary to code at all, only to define the steps of the pipeline and their parameters.
- **Portable**: Thanks to Docker, pipelines can be packaged in a Docker image along with their dependencies, making them really portable and reproducible. Using compi-dk, developers only need to complete the Dockerfile template provided with the dependencies that the pipeline needs. Notwithstanding, pipelines can be also run locally without Docker. 
- **User interface generation**: the Compi workflow engine automatically generates a Command-Line user interface to facilitate the users the usage of the pipeline. Thus, Compi is in fact an application framework in charge of dealing with user interaction, multi-threaded pipeline execution and logging, saving developers’ time with these aspects. Developers can focus in things that are really specific to their pipeline-based applications. 
- **Parallel execution**: Compi pipelines run independent tasks in parallel and pipeline users ande developers do not have to worry about parallel execution management. Pipeline execution can be resumed from any step, without repeating previous steps that may have completed in previous runs.  

The Compi ecosystem
===================

Compi is an ecosystem that comprises:

- ``compi``: the workflow engine with a command-line user interface to control the pipeline execution.
- ``compi-dk``: a command-line tool to help in the development and packaging of Compi-based applications.
- *Compi Hub*: a public repository of Compi pipelines that allows other users to discover, browse and reuse them easily.
