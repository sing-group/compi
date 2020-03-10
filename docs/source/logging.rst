.. _logging:

Logging
*******

By default, the standard and error outputs of each task executions are not saved. It is possible to save these outputs in log files by using the option ``--logs/-l </path/to/logs/directory>``. Since this option tells compi to create logs for all tasks, it is possible to select specific tasks to log with ``--log-only-task`` or ``--no-log-task``.

Run the following command to execute the ``pipeline.xml`` file using the example parameters file (``params``) generating logs in ``/tmp``.

.. code-block:: bash

 compi run -p pipeline.xml -pa params -l /tmp

 
.. note::

 With the ``-o`` parameter, the task stdout/stderr are forwarded to the compi stdout/stderr: ``compi run -p pipeline.xml -pa params -st task-8 -o``
