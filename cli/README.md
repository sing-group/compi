# Compi `cli`
The `cli` module provides a tool that allows you to run pipelines in your host machine. With this utility you can launch your pipeline entirely or partially as well as other functions related to pipelines. Check the [quickstart](QUICKSTART.md) to see some examples.

## Building `cli`
To build the `cli`, you can run:
- `mvn clean package -PcreateDist`: this command compiles the module and creates the compi distributable under `target/dist`.
- `mvn clean package -PcreateInstaller`: this command compiles the module and creates the compi self-extracting installer under `target/installer` (this option also creates the compi distributable under `target/dist`).

## `cli` commands
Run the `cli` (with `mvn exec` or `target/dist/compi`) to list the available commands:

```
usage: compi <command> [options]
where <command> is one of:
        run
                Runs a pipeline.
        validate
                Validates a pipeline.
        export-graph
                Exports a pipeline to a graph file.
Write 'compi help <command>' to see command-specific help
```

### `run`
```
Command run
usage: compi run -p <pipeline> [-pa <params>] [-t <num-threads>] [-s <skip>] [-st <single-task>]
        --pipeline/-p
                XML pipeline file
        --params/-pa
                XML params file
        --num-threads/-t
                number of threads to use (default: 6)
        --skip/-s
                skip to task. Runs the pipeline from the specific without running its dependencies. This option is incompatible with --single-task, --until and --before
        --single-task/-st
                runs a single task without its depencendies. This option is incompatible with --skip, --until and --before
        --until/-ut
                runs until a task (inclusive) including its depencendies. This option is incompatible with --single-task, --skip and --before
        --before/-bt
                runs all tasks which are dependencies of a given task. This option is incompatible with --single-task, --skip and --until
        --runners-config/-r
                XML file configuring custom runners for tasks. See the Compi documentation for more details
```

### `validate`

```
Command validate
usage: compi validate -p <pipeline>
        --pipeline/-p
                XML pipeline file
```

### `export-graph`
```
Command export-graph
usage: compi export-graph -p <pipeline> -o <output> [-f <format>] [-w <width>] [-h <height>] [-fs <font-size>]
        --pipeline/-p
                XML pipeline file
        --output/-o
                output file
        --format/-f
                graph format. Values: png, svg, xdot, json (default: png)
        --orientation/-or
                graph orientation. Values: horizontal, vertical (default: vertical)
        --width/-w
                graph width. By default, no width is used so the graph takes the minimum required. This option is incompatible with --height
        --height/-h
                graph height. By default, no height is used so the graph takes the minimum required. This option is incompatible with --width
        --font-size/-fs
                graph font size (default: 10)
```
