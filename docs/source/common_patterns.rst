.. _common_patterns:

Common patterns
***************

This section provides useful examples to define or run Compi pipelines that can
be reused across projects.

Resume pipeline execution in Docker images of Compi pipelines
=============================================================
As explained in the `Running pipelines` section, the ``compi resume`` command 
allows to resume the pipeline execution with the same parameters and running 
configuration as in the original execution performed with ``compi run``. For 
this, Compi stores one resume file for each different pipeline at 
``${HOME}/.compi``.

When a Compi pipeline is executed trough a Docker image
created with compi-dk, the home directory is the ``/root`` directory inside
the running container and therefore the files created in it are not persisted
across different executions. To persist this directory, the ``/root/.compi``
directory of the container can be mapped to a host directory using the ``-v``
parameter of ``docker run``. If mapped with ``-v ${HOME}/.compi:/root/.compi``,
then the resume files of the Dockerized pipelines will be stored at 
``${HOME}/.compi`` with the rest of the resume files.

Once the resume files are preserved, the ``compi resume`` command must be
executed when running the pipeline Docker image. Note that the default
`Dockerfile` defines the following entrypoint:

.. code-block:: xml

    ENTRYPOINT ["/compi", "run",  "-p", "/pipeline.xml"]

This means that every time the Docker image is executed, the 
``/compi run -p /pipeline.xml`` command is executed. To run a different command,
the ``--entrypoint`` parameter of ``docker run`` must be used to override then
entrypoint defined in the Dockerfile. As done before, the host directory 
containing the resume files must be also mounted:

.. code-block:: console

    docker run -v ${HOME}/.compi:/root/.compi --entrypoint "/compi" compi-test resume -p /pipeline.xml

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


