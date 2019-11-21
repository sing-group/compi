FAQ
***

Why does the log file of a Python task is not updated?
======================================================

By default, the Python's interpreter uses output buffering, which may cause that the log files of a Python task are not updated. This can be solved by:

- Passing the ``-u`` parameter to the Python's interpreter.
- Setting the ``PYTHONUNBUFFERED`` environment variable to ``TRUE`` or ``1``.

Does ``compi-dk`` require ``compi`` to be installed?
====================================================

No. When ``compi-dk`` is installed and used to develop pipelines, the ``build`` command creates a Docker image containing the pipeline and the ``compi`` executable. Then, the pipeline is executed using this ``compi`` inside a Docker container and, therefore, ``compi`` is not required to be also installed locally.

See the :ref:`Application Development<application_development>` section to see in detail how ``compi-dk`` works.
