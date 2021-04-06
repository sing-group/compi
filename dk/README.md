# Compi Development Kit
The `dk` module contains the Development Kit for Compi. This tool allows you to create pipelines to be run on docker. The docker image will contain (i) compi cli and compi core and (ii) all third-party programs and dependencies your pipeline needs. compi-dk helps you on creating and building this docker image with your *portable* pipeline and all its dependencies.

## Building `dk`
To build the `dk`, you can run:
- `mvn clean package -PcreateDist`: this command compiles the module and creates the compi-dk distributable under `target/dist`.
- `mvn clean package -PcreateInstaller`: this command compiles the module and creates the compi-dk self-extracting installer under `target/installer` (this option also creates the compi distributable under `target/dist`).

## `dk` commands
Run the `cli` (with `mvn exec` or `target/dist/compi-dk`) to list the available commands:

```
usage: compi-dk <command> [options]
where <command> is one of:
        new-project
                Creates a new compi project
        build
                Builds a compi project
        hub-init
                Creates a new pipeline at compi-hub
        hub-push
                Pushes a pipeline to compi-hub
        hub-metadata
                Sets the compi-hub pipeline version metadata
        list-tasks
                Lists pipeline tasks
        list-params
                Lists pipeline parameters
        create-metadata-skeleton
                Creates the metadata skeleton based on the current parameters
Write 'compi-dk help <command>' to see command-specific help
```

### `new-project`
```
Command new-project
usage: compi-dk new-project -p <path> -n <image-name> [-i <base-image>] [-v <compi-version>]
        --path/-p
                path of the new project
        --image-name/-n
                name for the docker image
        --base-image/-i
                base image for the docker image (default: ubuntu:16.04)
        --compi-version/-v
                compi version (default: 1.2.0)
```

### `build`

```
Command build
usage: compi-dk build [-p <path>]
        --path/-p
                path the new project to build (default: .)
```

### `hub-init`

```
Command hub-init
usage: compi-dk hub-init [-p <path>] -a <alias> -t <title> [-v] [-f]
        --path/-p
                path of the project (default: .)
        --alias/-a
                Alias of the pipeline
        --title/-t
                Title of the pipeline
        --visible/-v
                Whether the pipeline is publicly visible or not
        --force/-f
                Whether the pipeline alias should be overriden. Note that this option will create a new pipeline at compi-hub and update the alias associated to this project
```

### `hub-push`

```
Command hub-push
usage: compi-dk hub-push [-p <path>] [-v] [-f]
        --path/-p
                path of the project (default: .)
        --visible/-v
                make the pipeline version visible at Compi Hub
        --force/-f
                replace the previous version at Compi Hub
```

### `hub-metadata`

```
Command hub-metadata
usage: compi-dk hub-metadata [-p <path>]
        --path/-p
                path of the project (default: .)
```

### `list-tasks`

```
Command list-tasks
usage: compi-dk list-tasks [-p <pipeline>] [-c]
        --pipeline/-p
                XML pipeline file (default: pipeline.xml)
        --complete-report/-c
                Use this flag to print a complete report
```

### `list-params`

```
Command list-params
usage: compi-dk list-params [-p <pipeline>] [-c]
        --pipeline/-p
                XML pipeline file (default: pipeline.xml)
        --complete-report/-c
                Use this flag to print a complete report
```

### `create-metadata-skeleton`

```
Command create-metadata-skeleton
usage: compi-dk create-metadata-skeleton [-p <pipeline>]
        --pipeline/-p
                XML pipeline file (default: pipeline.xml)
```
