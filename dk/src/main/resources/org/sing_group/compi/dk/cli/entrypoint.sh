#!/bin/bash

if [ $# -gt 0 ] && [ "$1" == "resume" ]; then
  /compi resume -p /pipeline*.xml "${@:2}"
else
  /compi run -p /pipeline*.xml "$@"
fi
