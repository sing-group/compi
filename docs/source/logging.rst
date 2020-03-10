.. _logging:

Logging
*******

By default, the standard and error outputs of each task execution are not saved. In this section you will find how to
tell Compi to save and/or show these outputs.

Show task standard outputs 
==========================

You can capture and forward task standard outputs to Compi, so you will be able to see their output easily. You have to
simply provide the ``-o`` parameter. For example: 

.. code-block:: bash

 compi run -p pipeline.xml -o

Log task outputs into files
===========================

It is possible to save task outputs in log files by using the option ``--logs/-l </path/to/logs/directory>``.

Run the following command to execute the ``pipeline.xml`` file using the example parameters file (``params``) generating logs in ``/tmp``.

.. code-block:: bash

 compi run -p pipeline.xml -pa params -l /tmp

Log only specific tasks
=======================

Since this option tells Compi to create logs for all tasks, it is possible to select specific tasks to log with ``--log-only-task``.
These option should be provided multiple times, one for each task. For example:

.. code-block:: bash

 compi run -p pipeline.xml -pa params -l /tmp --log-only-task task-1 --log-only-task task-2

Exclude tasks from logging
==========================

You can also exclude tasks from being logged with ``--no-log-task``. As in ``--log-only-task``, you have to provide this option
multiple times, one per excluded task. For example:

.. code-block:: bash

 compi run -p pipeline.xml -pa params -l /tmp --no-log-task task-1 --no-log-task task-2


