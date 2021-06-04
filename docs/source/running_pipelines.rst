Running pipelines
*****************

This section explains how to use ``compi`` to run pipelines using different parameters of the ``compi run`` command by showing several practical examples.

.. _sample_pipeline:

Sample pipeline
---------------

The sample pipeline used in this section is available here (http://static.sing-group.org/software/compi/downloads/manual/sample-pipeline.zip).

This is the XML definition of the pipeline:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0</version>
        <params>
            <param name="path" shortName="p2">A path to a program to execute</param>
            <param name="seconds" shortName="se">The seconds parameter</param>
            <param name="name" shortName="n">The name parameter</param>
        </params>
        <tasks>
            <foreach id="task-1"
                of="list" in="3,7,1,1" as="nonParamSeconds" params="path name">
                ${path} ${name} ${nonParamSeconds}
            </foreach>
            <task id="task-2" after="task-1" params="path">
                ${path} p2 2
            </task>
            <task id="task-3" after="task-1" params="path">
                ${path} p3 2
            </task>
            <task id="task-4" after="task-2" params="path seconds">
                ${path} p4 ${seconds}
            </task>
            <task id="task-5" after="task-2" params="path">
                ${path} p5 2
            </task>
            <task id="task-6" after="task-3" params="path seconds">
                ${path} p6 ${seconds}
            </task>
            <foreach id="task-7" after="task-5,task-6" of="list" in="1,1,5" as="nonParamSeconds" params="path seconds">
                ${path} p8 ${nonParamSeconds}
            </foreach>
            <task id="task-8" after="task-7" params="path">
                ${path} p9 2
            </task>
        </tasks>
    </pipeline>
    
This is the parameters file:

.. code-block:: xml

 path=./execute.sh
 seconds=2
 name=p1
 
And this is the ``execute.sh`` script referenced in the parameters file (make it executable with chmod +x execute.sh):

.. code-block:: bash

 #!/bin/bash
 echo "Starting $1"
 sleep $2
 echo "Finishing $1"
 
Executing the pipeline using a parameters file
----------------------------------------------

Run the following command to execute the ``pipeline.xml`` file using the example parameters file (``params``):

.. code-block:: bash

 compi run -p pipeline.xml -pa params
  
.. note::
  
  When the name of the XML is ``pipeline.xml``, it can be ommited from the command as Compi will automatically look for it: ``compi run -pa params``

Executing the pipeline using command-line pipeline parameters
-------------------------------------------------------------

Pipeline parameters can be also indicated in the command line, separated by a ``--`` after the ``compi run`` parameters.

Run the following command to execute the ``pipeline.xml`` file providing the required parameters trough the command-line:

.. code-block:: bash
 
 compi run -p pipeline.xml -- --path .execute.sh --name command-line-name --seconds 1

Resuming a pipeline
-------------------

If a pipeline execution is interrupted (such pressing CTRL-C, electrical failure, or some tasks were aborted), it can be resumed through the
command-line:

.. code-block:: bash
 
 compi resume -p pipeline.xml

The execution will resume the pipeline execution with the **same parameters and running configuration** as in the original execution
performed with ``compi run``.

All tasks that should run did not completed successfully are started again.

.. note::

 The pipeline file cannot be changed. If it is changed, an error is shown. However, you can skip this check at your
 own risk and force Compi to resume the pipeline by using a modified version of the pipeline file.

 ``compi resume --flexible -p pipeline.xml``

.. note::
 If you are running the pipeline using Docker, please refer to :ref:`resume in docker <resume_in_docker>`

Advanced execution control
--------------------------

For gaining control of the execution process, you can se the advanced topics, such as :ref:`partial execution <partial_execution>`, :ref:`logging <logging>`, etc.

