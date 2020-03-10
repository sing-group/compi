Running Pipelines
*****************

This section explains how to use ``compi`` to validate and run pipelines using different parameters of the ``compi run`` command by showing several practical examples.

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
 
Validating a pipeline
---------------------

Run the following command to validate the ``pipeline.xml`` file:

.. code-block:: bash

 compi validate -p pipeline.xml

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

Export the pipeline graph as an image
-------------------------------------

Run the following command to export the graph defined by the ``pipeline.xml`` pipeline as an image.

.. code-block:: bash

 compi export-graph -p pipeline.xml -o pipeline.png -f png

.. figure:: images/writing/pipeline.png
   :align: center
 
If you want to draw also the task parameters, try options ``--draw-task-params`` or ``--draw-pipeline-params``.

Executing the pipeline using a custom task runner
-------------------------------------------------

It is possible to run pipeline tasks using :ref:`custom runners<custom_runners>`, which must be defined in XML passed with the ``-r`` or ``--runners-config`` parameter. This mode is meant to allow users to run tasks using ways different than the default ``/bin/sh -c`` used by Compi, such as running tasks in Docker images or using work managers (e.g. Slurm, qsub).

This is the XML file with the runners definition provided in the sample pipeline:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner>
            echo "[${task_id}] code: ${task_code}" >> /tmp/runner-output.txt
            /bin/sh -c "${task_code}"
        </runner>
    </runners>

Run the following command to execute the ``pipeline.xml`` file using the example parameters file (``params``) with the custom runner defined in the ``pipeline-runner.xml`` file. This runner simply writes a log in ``/tmp/runner-output.txt`` and runs each task using ``/bin/sh -c``.

.. code-block:: bash

 compi run -p pipeline.xml -pa params -r pipeline-runner.xml
