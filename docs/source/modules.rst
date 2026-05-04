.. _modules:

Modules
*******

A Compi module is a reusable, shareable extension that can be added to an existing Compi project in a standardized way. Unlike systems where modules are treated as external dependencies, Compi allows users to vendor modules directly into their projects: when ``compi-dk add-module`` is used, the module source is merged directly into the project. There are no black boxes — the resulting code is fully editable and becomes an integral part of the codebase.

Module structure
================

A Compi module is distributed as a ZIP package. The package must contain the following mandatory files:

- ``metadata.json``: a JSON file whose ``name`` and ``version`` fields must be non-empty. An optional ``description`` field may also be included.
- ``LICENSE_<modulename>``: a text file containing the module license.

The following files are optional. If absent, they are treated as empty or ignored:

- ``tasks.xml``: tasks to be inserted before ``</tasks>`` in the target pipeline file.
- ``parameters.xml``: parameters to be inserted before ``</params>`` in the target pipeline file.
- ``metadata.xml``: task descriptions to be inserted before ``</metadata>`` in the target pipeline file.
- ``runners.xml``: runners to be inserted before ``</runners>`` in the target runners file (only applied if a runners file exists in the project).
- ``Dockerfile``: Dockerfile instructions that are appended to the project Dockerfile with a ``# Module: <name>`` comment (only applied if a project Dockerfile exists).
- ``bundles/``: any ``.zip`` files placed directly inside this directory are extracted into the project root. If ``bundles/`` contains subdirectories, their contents are repackaged into a single generated ZIP (``bundles-content.zip``) preserving relative paths, which is then extracted into the project root.
- ``files.txt``: each non-empty line is treated as a URL pointing to a ZIP file. Each downloaded ZIP is extracted into the project root. As with ``bundles/``, relative paths inside the ZIPs are preserved, so the module developer controls the final destination through the ZIP's internal directory structure.

Adding a module to a project
=============================

The ``compi-dk add-module`` command integrates a module into an existing Compi project by providing a URL to a ZIP package:

.. code-block:: bash

 compi-dk add-module --url https://.../module.zip

The tool performs the following steps:

1. Downloads and extracts the ZIP package.
2. Validates the module metadata (``metadata.json``).
3. Registers the module identity in the ``compi.project`` file.
4. Merges the module-provided pipeline components (tasks, parameters, metadata, and runner definitions) into the corresponding project files.
5. Appends any Dockerfile instructions to the project Dockerfile (if present).
6. Deploys any bundled or downloaded resources into the project root.

Available modules
=================

Two modules have been developed to illustrate this feature.

init-working-dir
----------------

The ``init-working-dir`` module provides the tooling needed to bootstrap a working directory for a Compi-based pipeline (https://github.com/sing-group/compi-module-init-working-dir). 

It bundles a set of template files — including a parameters file, a pipeline launcher script, and an empty ``README.txt`` — directly into the Docker image. It exposes an ``init_working_dir.sh`` script that copies those templates into a user-specified directory. Pipeline developers must then edit and adapt this script to their specific case. This gives end users an immediately runnable starting point, with a pre-configured parameters file and a convenience script that handles the ``docker run`` invocation required to execute the pipeline. 

docker-management
-----------------

The ``docker-management`` module provides reusable pipeline tasks for managing Docker images within a Compi workflow (https://github.com/sing-group/compi-module-docker-management). It includes only ``metadata.json``, ``parameters.xml``, and ``tasks.xml``.

The module introduces a naming convention where any pipeline parameter whose name matches the pattern ``<task_id>_docker_image`` is automatically recognised as a Docker image reference for the corresponding ``task_id``. Using this convention, the module supplies two tasks:

- ``pull_docker_images``: iterates over all parameters matching the convention and pulls any image not already present in the local Docker registry.
- ``cleanup_docker_images``: removes all locally cached images referenced by those parameters.

The module also declares an example parameter (``hello_world_docker_image``) to illustrate the expected naming pattern. Pipeline developers may create one parameter per task with the default Docker image that must be downloaded. After adding the module, developers need to edit the remaining tasks in ``pipeline.xml`` to declare that they should run after ``pull_docker_images``, and to update ``cleanup_docker_images`` to declare its predecessors.

Quickstart example
==================

The following example creates a new ``compi-dk`` project and adds the ``docker-management`` module using its GitHub tag URL. GitHub automatically generates these URLs for each tag or release, providing a ZIP of the entire repository, which follows the structure of a Compi module:

.. code-block:: bash

 COMPI_DK_PROJECT=$(mktemp -d /tmp/compi-dk-test.XXXXX)

 cd ${COMPI_DK_PROJECT}

 compi-dk new-project --path test_project --image-name compi_test_modules_dev --base-image ubuntu:24.04

 cd test_project

 compi-dk add-module --url https://github.com/sing-group/compi-module-docker-management/archive/refs/tags/v1.1.0.zip

 cat compi.project
