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

Use Docker images from a Docker image of a Compi pipeline
=========================================================

Sometimes, pipelines include ``docker run`` commands in their tasks to run external dependencies or manage their execution using an external runners file that also use Docker containers. When such pipelines are distributed as Docker images, the corresponding pipeline Docker images must be able to run other Docker images. To do so, the following considerations must be taken when implementing and executing a pipeline:

- The main Docker image must have a Docker installation. This can be achieved by using as base image one that has Docker installed (e.g. the ``pegi3s/docker`` image from the Bioinformatics Docker Images Project [https://pegi3s.github.io/dockerfiles/]).
- When the pipeline is executed, the Docker daemon socket must be mounted (``-v /var/run/docker.sock:/var/run/docker.sock``).
- Child containers must access the pipeline data, and therefore the pipeline data directories must be mounted properly in these child containers. It is important to note that these must be host paths, since the child containers run in the host. Below are explained two ways to mount paths properly.

Consider the following pipeline that uses a foreach task to align all FASTA files in a given directory (specified by the ``input`` parameter) and generates the output files in a different directory (specified by the ``output`` parameter). The alignment tool is Clustal Omega, which is executed using a Docker image. To run this child image, the input and output directories are mounted at ``/input`` and ``/output``, respectively. The compi-dk project of this pipeline is available here (http://static.sing-group.org/software/compi/downloads/manual/compi-docker-1.zip).

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>

    <!-- This is an example file of a compi pipeline -->
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>0.0.1</version>

        <params>
            <param name="input" shortName="i">Directory containing input files.</param>
            <param name="output" shortName="o">Directory to put the output files.</param>
        </params>

        <tasks>
            <foreach id="align" of="file" in="${input}" as="file" params="input output">
                filename=$(basename -- "$file")
                echo "Align ${filename}"
                docker run --rm \
                    -v ${input}:/input \
                    -v ${output}:/output \
                    pegi3s/clustalomega -i /input/${filename} -o /output/${filename}
            </foreach>
        </tasks>
    </pipeline>

To run this pipeline (without building a Docker image for it), simply use ``compi run -o -- --input $(pwd)/input --output $(pwd)/output``. 

Now, let's create a Docker image for the same pipeline using ``compi-dk build -drd``. When running this image, we must take into account that the child container used to run Clustal Omega will be executed in the host, and thus the input and output paths that the pipeline will use (that are pipelines in its Docker container) must also exist in the host. The simplest way to mount paths properly is to map the host paths into the pipeline container with the same path, thus the pipeline execution is as follows:

.. code-block:: console

    docker run --rm \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v $(pwd):$(pwd) \
        compi-test \
            -o -- --output $(pwd)/output --input $(pwd)/input
    
As an alternative to this approach, the pipeline can be redefined to specify a main working directory and the name of the input and output directories relative to it. Pipelines using this approach, usually declare another parameter to indicate the host working directory, which is used in the tasks to mount the host working directory when running other Docker images. This way, the pipeline working directory can be a different path that only exists in the pipeline image (e.g. in this case, it has ``/working_dir`` as default value). The compi-dk project of this pipeline is available here (http://static.sing-group.org/software/compi/downloads/manual/compi-docker-2.zip).

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>

    <!-- This is an example file of a compi pipeline -->
    <pipeline xmlns="http://www.sing-group.org/compi/pipeline-1.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <version>0.0.1</version>

        <params>
            <param name="host_working_dir" shortName="hw" global="true">Path of the working directory in the host.</param>
            <param name="working_dir" shortName="w" global="true" defaultValue="/working_dir">Path of the working directory.</param>
            <param name="input" shortName="i">Directory containing input files (relative to working_dir).</param>
            <param name="output" shortName="o">Directory to put the output files (relative to working_dir).</param>
        </params>

        <tasks>
            <foreach id="align" of="file" in="${working_dir}/${input}" as="file" params="input output">
                filename=$(basename -- "$file")
                echo "Align ${filename}"
                docker run --rm \
                    -v ${host_working_dir}/${input}:/input \
                    -v ${host_working_dir}/${output}:/output \
                    pegi3s/clustalomega -i /input/${filename} -o /output/${filename}
            </foreach>
        </tasks>
    </pipeline>

Within this approach, the pipeline is run as follows. Note that the host working directory (``$(pwd)``) is mounted into ``/working_dir`` (the default value of the ``working_dir`` parameter of the pipeline, used inside the pipeline Docker image) and passed to the ``--host_working_dir`` parameter.
    
.. code-block:: console
    
    docker run --rm \
        -v /var/run/docker.sock:/var/run/docker.sock \
        -v $(pwd):/working_dir \
        compi-test-2 \
            -o -- --host_working_dir $(pwd) --output output --input input

The `GenomeFastScreen <https://www.sing-group.org/compihub/explore/5e2eaacce1138700316488c1>`_ and `IPSSA <https://www.sing-group.org/compihub/explore/5fa91806407682001ad3a1e9>`_ pipelines, publicly available at CompiHub, were implemented following this second approach.
