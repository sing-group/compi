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

### `validate`

```
Command build
usage: compi-dk build [-p <path>]
        --path/-p
                path the new project to build (default: .)
```