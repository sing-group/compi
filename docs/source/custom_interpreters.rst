Custom interpreters
********************

Interpreters are the way to use **other languages** for tasks' code **directly**
in the pipeline XML. By default, tasks' code are interpreted with bash, however
you can define a custom `interpreter` for any task.

How to define an interpreter for a task
=======================================
You need to add the ``interpreter`` attribute to the ``<task>`` element whose
code will be written in another language. The interpreter attribute takes a 
bash script that will be run **instead of** the task code. You will then need
to run the task code manually (normally by means of an interpreter, such as
python, awk, etc.).

Inside this script you will have available all the task's parameters as
environment variables, as well as:

- ``$task_code``. The task's code. You can send this code to the correct interpreter.
- ``$task_id``. The task's ``id``.
- ``$task_interpreter``. The task's ``interpreter`` itself.
- ``$task_params``. A list of parameters of the task, separated by white-spaces.



Examples
========


Consider the following ``<task>`` definition:

.. code-block:: xml

 <task id="copy-lines" 
    params="input_file output_file_awk" 
    interpreter="/usr/bin/awk -e '$task_code' $input_file > $output_file_awk">
  {
    print $1
  }
 </task>

In this ``<task>``, the interpreter is: ``/usr/bin/awk -e '$task_code' $input_file > $output_file_awk``. When the task is about to run, the code that runs is simply
that specified in ``interpreter``, which in turn runs the AWK interpreter
against the task's code (``-e '$task_code'``), and uses the ``$input_file``
and ``$output_file`` parameters of the task to feed the AWK script and to save
its output, respectively.
  

Now consider the following example:

.. code-block:: xml

    <task id="write-parameter" params="input_file output_file_python"
        interpreter="/usr/bin/python -c &quot;$task_code&quot;" >
      import os
      f = open(os.environ['output_file_python'], "a")
      f.write(os.environ['input_file'])
    </task>
    


Here, the interpreter is: ``/usr/bin/python -c &quot;$task_code&quot;``
which simply uses the Python interpreter to run the tasks code (``-c &quot;$task_code&quot;``). Inside the task's
code, you can see how to access the task's parameters via the process environment
(``os.environ``, remember that parameters in Compi are passed to the process as environmental variables).

.. note::
  
  Regarding **portability**, it is recommended that you use interpreters
  that are available in the majority of operating systems, in order to make your
  pipeline portable.