Custom interpreters
********************

What are Compi `interpreters`
=============================

Interpreters are the way to use **other languages** for tasks' code **directly**
in the pipeline XML. By default, tasks' code are interpreted with bash, however
you can define a custom `interpreter` for any task.

How to define an interpreter for a task
=======================================
You need to add the ``interpreter`` attribute to the ``<task>`` element whose
code will be written in another language. The interpreter attribute takes a 
bash script that will be run **instead of** the task code. Inside this script
you will have available all the task's parameters as environment variables,
as well as:

- ``$task_code``. The task's code. You can send this code to the correct interpreter.
- ``$task_id``. The task's ``id``.
- ``$task_interpreter``. The task's ``interpreter`` itself.
- ``$task_params``. A list of parameters of the task, separated by white-spaces.



Example
=======
Here there is an example with a custom interpreter of a task.

.. code-block:: xml

  <pipeline xmlns="http://www.esei.uvigo.es/compi-pipeline"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <params>
          <param name="input_file" shortName="i">File for awk</param>
          <param name="output_file_awk" shortName="oawk">File for output</param>
          <param name="output_file_python" shortName="opython">File for output</param>
      </params>
      <tasks>
          <task id="ID-1" 
                params="input_file output_file_awk" 
                interpreter="/usr/bin/awk -e '$task_code' $input_file > $output_file_awk">
              {
                print $1
              }
          </task>
          <task id="ID-2" params="input_file output_file_python"
                interpreter="/usr/bin/python -c &quot;$task_code&quot;" >
              import os
              f = open(os.environ['output_file_python'], "a")
              f.write(os.environ['input_file'])
          </task>
      </tasks>
  </pipeline>

.. note::
  
  A note regarding **portability**. It is recommended that you use interpreters
  that are available in the majority of operating systems, in order to make your
  pipeline portable.