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
sage: compi run -p <pipeline> [-pa <params>] [-n <num-tasks>] [-l <logs>] [-lt <log-only-task>] [-nl <no-log-task>] [-st <single-task>] [-f <from>] [-a <after>] [-ut <until>] [-bt <before>] [-r <runners-config>] [-o] [-q] [-w]
        --pipeline/-p
                XML pipeline file
        --params/-pa
                parameters file
        --num-tasks/-n
                maximum number of tasks that can be run in parallel. This is not equivalent to the number of threads the pipeline will use, because some tasks can be parallel processes themselves (default: 6)
        --logs/-l
                Directory to save tasks' output (stdout and stderr, in separated files). By default, no output is saved. If this option is provided, all task's output will be logged by default. You can select which tasks to log with --log-only-task or --no-log-task
        --log-only-task/-lt
                Log task(s). Task id(s) whose output will be logged, other tasks' output will be ignored. This parameter is incompatible with --no-log-task. If you use this option, you must provide a log directory with --logs. This option can be specified multiple times
        --no-log-task/-nl
                Do not log task(s). Task id(s) whose output will be ignored, other tasks' output will be saved. This parameter is incompatible with --log-only-task. If you use this option, you must provide a log directory with --logs. This option can be specified multiple times
        --single-task/-st
                runs a single task without its depencendies. This option is incompatible with --from, --after, --until and --before
        --from/-f
                from task(s). Runs the pipeline from the specific task(s) without running its/their dependencies. This option is incompatible with --single-task. This option can be specified multiple times
        --after/-a
                after task(s). Runs the pipeline from the specific task(s) without running neither it/them nor its/their dependencies. This option is incompatible with --single-task. This option can be specified multiple times
        --until/-ut
                runs until a task (inclusive) including its depencendies. This option is incompatible with --single-task and --before
        --before/-bt
                runs all tasks which are dependencies of a given task. This option is incompatible with --single-task and --until
        --runners-config/-r
                XML file configuring custom runners for tasks. See the Compi documentation for more details
        --show-std-outs/-o
                Forward task stdout/stderr to the compi stdout/stderr
        --quiet/-q
                Do not output compi logs to the console
        --abort-if-warinings/-w
                Abort pipeline run if there are warnings on pipeline validation
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
usage: compi export-graph -p <pipeline> -o <output> [-f <format>] [-or <orientation>] [-w <width>] [-h <height>] [-fs <font-size>] [-lw <line-width>] [-tc <task-colors>] [-te <task-styles>] [-dpp] [-dtp] [-it <include-task-params>] [-et <exclude-task-params>]
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
        --line-width/-lw
                the line width of the graph nodes (default: 1)
        --task-colors/-tc
                the colors to the draw the task nodes. Colors must be specified using their corresponding hexadecimal codes. Use the following format: task-id-1:color;task-id-2,task-id-3:color
        --task-styles/-te
                the styles to the draw the task nodes. Use the following format: task-id-1:style;task-id-2,task-id-3:style. Possible values for styles: dashed, solid, invis, bold, filled, radial, diagonals, rounded
        --draw-pipeline-params/-dpp
                use this flag to draw one node for each pipeline parameter. Each parameter node will be connected to the tasks using them. This flag is incompatible with --draw-task-params
        --draw-task-params/-dtp
                use this flag to draw one node for each task with all the task parameters. This flag is incompatible with --draw-pipeline-params
        --include-task-params/-it
                when draw parameters options (draw-pipeline-params or draw-task-params) are used, this option specifies the tasks for which parameter nodes should be created or parameters should be linked to. Task identifiers must be separated by commas. This option is incompatible with --exclude-task-params
        --exclude-task-params/-et
                when draw parameters options (draw-pipeline-params or draw-task-params) are used, this option specifies the tasks for which parameter nodes should not be created or parameters should not be linked to. Task identifiers must be separated by commas. This option is incompatible with --include-task-params
```

### `help-task`
```
Command help-task
usage: compi help-task -p <pipeline> -t <task>
        --pipeline/-p
                XML pipeline file
        --task/-t
                Task identifier
```
