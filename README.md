![Compi Logo](artwork/logo.png)
# Compi: framework for portable computational pipelines

## What is Compi?
Compi is an application development framework for portable computational pipelines.
A [software pipeline](https://en.wikipedia.org/wiki/Pipeline_(software)) is a
chain of processing elements so that the output of each element is the input 
of the next.

In Compi, processing elements (called *tasks*) are external
programs written in any programming language. In addition, Compi supports pipeline
branching, where *tasks* can be run *parallel* with others. Moreover, a pipeline
can be run partially, starting from a given intermediate point.

In addition, Compi allows you to develop **portable** pipelines, thanks to [Docker](http://docker.io).

Compi pipelines are defined in XML files, such as this one:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!-- This is an example file of a compi pipeline -->
<pipeline xmlns="http://www.esei.uvigo.es/compi-pipeline"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<params>
		<param name="parameter1" shortName="p1">Parameter one</param>
		<param name="parameter2" shortName="p2">Parameter two</param>
		<param name="a-list" shortName="l">A list of comma separated values</param>
		<param name="out" shortName="o">Out file</param>
	</params>

	<tasks>
		<task id="task-1">
			echo ${parameter1} >> ${out}
			echo ${parameter2} >> ${out}
		</task>
		<foreach id="task-2" after="task-1" of="param" in="a-list" as="i">
			echo ${i} >> ${out}
		</foreach>
	</tasks>

</pipeline>
```

## Requisites
* Linux
* Java JDK
* Maven
* Docker

## Building
```
# Clone the repository
git clone https://github.com/sing-group/compi.git
cd compi
mvn package
```
Inside `dk/target/installer` you will find `compi-dk-<version>-installer.bsx`,
which is a self-extracting installer of the *Compi Development Kit*, compatible
with any Linux 64-bit.

```
sudo ./dk/target/installer/compi-dk-<version>-installer.bsx
```

## Getting started
The first step is to run `compi-dk` to create a project:

```
compi-dk new-project -p ~/my-new-pipeline -n my-new-pipeline
```

The option `-p` indicates the project location and the `-n` option indicates
the name of the Docker image. Now you have a new pipeline project inside `~/my-new-pipeline`

To build the pipeline:
```
cd ~/my-new-pipeline
compi-dk build
```

Now you have a new Docker image `my-new-pipeline`

To run the image you have to run `docker run <docker-params> my-new-pipeline [<pipeline-params>|-pa <pipeline-params-file>`. For example:
```
docker run -v /tmp:/data my-new-pipeline -p1 param-one -p2 param-two -o /data/output.txt -l one,two,three
cat /tmp/output.txt
```







