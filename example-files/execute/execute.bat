@echo off
echo Starting %1
ping -n %2 127.0.0.1 > nul
echo Finishing %1
