#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
java -jar "$DIR/${project.artifactId}-${project.version}-jar-with-dependencies.jar" $@
