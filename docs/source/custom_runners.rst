Custom runners
**************

What are Compi `runners`
========================

By default, compi runs task code by spawning local processes. With `runners`,
task' codes are passed to custom-made scripts which are in charge of running
them, for example, by submitting a job to a queue (e.g. Slurm, SGE) or using 
Docker images.

Runners are passed to the main ``compi run`` command using the ``-r`` 
parameter.

Creating a custom runner
========================

Like pipelines, runners are defined in XML. Individual runners are defined 
using the ``runner`` tag inside the ``runners`` tag. The ``task`` attribute 
is used to specify the list of tasks (comma-separated) that the corresponding 
runner must execute.

.. code-block:: xml

 <?xml version="1.0" encoding="UTF-8"?>
 <runners xmlns="http://sing-group.org/compi/runners-1.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <runner tasks="task-1, task-2">
         /bin/sh -c "${task_code}"
    </runner>
 </runners>

The runner code will have the following environment variables provided by compi:

- ``task_id``: contains the id of the task being executed.
- ``task_code``: contains the code (defined in the ``pipeline.xml``) of the task being executed.
- ``task_params``: contains the list of params associated to the task being executed.
- ``i``: in the case of ``foreach`` tasks, the iteration value.
- Like in regular Compi tasks, the task variables are also defined.

A simple example
----------------

Consider the following XML of the greetings pipeline:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0</version>
        <params>
            <param name="yourName" shortName="n" global="true" defaultValue="anonymous">Your name</param>
            <flag name="sayGoodBye" shortName="g">Do you want to say goodbye?</flag>
        </params>
        <tasks>
            <task id="greetings">
                echo "Hi ${yourName}"
            </task>          
            <task id="goodbye" 
                params="yourName sayGoodBye" if="[ -v sayGoodBye ]"
                after="greetings">
                echo "Goodbye ${yourName}"
            </task>
        </tasks>
    </pipeline>

And the following runners file where one runner is defined for the two 
pipeline tasks:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="greetings, goodbye">
            echo -e "[${task_id}] \n\tyourName: ${yourName} \n\tcode: ${task_code} \n\tparams: ${task_params}" >> /tmp/runner-output
            /bin/sh -c "${task_code}"
        </runner>
    </runners>

What this runner does is: printing task information (using the environment runner 
variables) into a file (``/tmp/runner-output``) and then running the task 
using the shell interpreter. This example can be executed with: 

.. code-block:: console

 compi run -p pipeline.xml -r runner.xml -o -- --sayGoodBye
 cat /tmp/runner-output

Examples of useful runners
==========================

Generic Docker runner
---------------------

Let's supose the following pipeline with one task to align a FASTA file using
Clustal Omega:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0</version>
        <params>
            <param name="workingDir" shortName="w" global="true">Working directory.</param>
            <param name="input" shortName="i" global="true">Input file.</param>
            <param name="output" shortName="o" global="true">Output file.</param>
            <param name="clustalomega" shortName="o" global="true" defaultValue="clustalo">Clustal Omega executable.</param>
        </params>
        <tasks>
            <task id="align">
                ${clustalomega} -i ${workingDir}/${input} -o ${workingDir}/${output}
            </task>          
        </tasks>
    </pipeline>

One may want to run this task using a Docker runner which runs the same task 
code inside a Docker container where the Clustal Omega executable is available.
The following runners file shows a runner to do this:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="align">
            envs=$(for param in $task_params; do echo -n "-e $param "; done)        
            docker run --rm $envs -v ${workingDir}:${workingDir} --entrypoint /bin/bash pegi3s/clustalomega -c "${task_code}"
        </runner>
    </runners>

The key points of this generic Docker runner are:

- The first line creates a variable with the list of parameters that should be passed to the Docker container as environment variables.
- The second line runs the docker image passing this list of environment variables and mounts the directory where the command has the input and output files.
- Since this particular image of Clustal Omega has an entrypoint defined, it must be overriden to run the desired task code.

Generic Slurm runner
--------------------

The following runners file shows a generic Slurm runner:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <runners xmlns="http://sing-group.org/compi/runners-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <runner tasks="task-1">
            tmpfile=$(mktemp /tmp/compi-task-code.XXXXXXXX)
            echo "#!/bin/bash" >> ${tmpfile}
            echo ${task_code} >> ${tmpfile}
            chmod u+x ${tmpfile}
            srun -c 1 -p main --export ALL -o /tmp/task-1.log -e /tmp/task-1.err -J task_1 bash ${tmpfile}
    </runner>
    
Some parameters of the ``srum`` may need to be adjusted for each specific
cluster, but this is how a generic Slurm runner may look like. The 
``export`` parameter must be used to export all the environment variables to
the process that will be executed, and this is neccessary because the task
parameters are declared as environment variables.
