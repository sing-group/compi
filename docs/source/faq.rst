FAQ
***

.. _python_log:

Why does the log file of a Python task is not updated?
======================================================

By default, the Python's interpreter uses output buffering, which may cause that the log files of a Python task are not updated. This can be solved by:

- Passing the ``-u`` parameter to the Python's interpreter.
- Setting the ``PYTHONUNBUFFERED`` environment variable to ``TRUE`` or ``1``.

Does ``compi-dk`` require ``compi`` to be installed?
====================================================

No. When ``compi-dk`` is installed and used to develop pipelines, the ``build`` command creates a Docker image containing the pipeline and the ``compi`` executable. Then, the pipeline is executed using this ``compi`` inside a Docker container and, therefore, ``compi`` is not required to be also installed locally.

See the :ref:`Application Development<application_development>` section to see in detail how ``compi-dk`` works.

.. _optional_parameter:

How to make a parameter optional?
=================================

Compi does not include any special keyword for optional pipeline parameters. To do that, just declare it with ``defaultValue=""``. This way, if the parameter has a value when the pipeline is executed, it means that it have been provided by the quser. Otherwise, if it is empty (i.e. it has the default value), it means that it has not been provided by the user.

Below is an example of a task that is only executed when the optional parameter ``file`` has a value:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>1.0.0</version>

        <params>
            <param name="file" shortName="f" defaultValue="">Optionally, a file.</param>
        </params>
        <tasks>
            <task id="test" params="file" if="test ! -z ${file}">
                echo "Task running to analyze file: ${file}"
            </task>
        </tasks>
    </pipeline>
