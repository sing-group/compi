.. _common_patterns:

Common patterns
***************

This section provides useful examples to define or run Compi pipelines that can
be reused across projects.

Run a task only if a directory exists and have files
====================================================
Imagine you need to run a task only when a given directory exists (``/tm/test``
in the example) and has files inside. This can be done using the ``if``
attribute to declare the task as follows:

.. code-block:: xml

    <task id="test" if="[ -d /tmp/test ] &amp;&amp; [ ! -z &quot;$(ls -A /tmp/test)&quot; ]">
        ls /tmp/test
    </task>

Now, if the directory does not exist and you run the pipeline with 
``compi run -o -p pipeline.xml``, you will see that the `test` task is not 
executed. If you create the directory and put some files in it, you will see 
that the task is then executed.
