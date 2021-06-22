![Compi Logo](artwork/logo.png)
# Compi: framework for portable computational pipelines [![license](https://img.shields.io/github/license/sing-group/compi)](https://github.com/sing-group/compi) [![release](https://img.shields.io/github/release/sing-group/compi.svg)](http://www.sing-group.org/compi) [![DOI](https://img.shields.io/badge/DOI-10.7717%2Fpeerj--cs.593-blue)](https://doi.org/10.7717/peerj-cs.593)

Compi is an application development framework for portable computational pipelines. A software pipeline is a chain of processing elements so that the output of each element is the input of the next.

There are many fields where computational pipelines constitute the main architecture of applications, such as big data analysis or bioinformatics.

Many pipelines combine third party tools along with custom made processes, conforming the final pipeline. Compi is the framework helping to create the final, portable application.

You can get more information at:

* Compi homepage: http://sing-group.org/compi
* Compi documentation: http://sing-group.org/compi/docs
* Compi Hub: http://sing-group.org/compihub
* Compi source code: https://github.com/sing-group/compi

# The Compi ecosystem

Compi is an ecosystem that comprises:

- `compi`: the workflow engine with a command-line user interface to control the pipeline execution.
- `compi-dk`: a command-line tool to help in the development and packaging of Compi-based applications.
- *Compi Hub*: a public repository of Compi pipelines that allows other users to discover, browse and reuse them easily.

# Install `compi` and `compi-dk`

## From binaries

Binaries for `compi` and `compi-dk` for Linux 64-bit systems are available here: https://www.sing-group.org/compi#downloads

Portable versions (*.tar.gz*) and self-extracted installers (*.bsx*) are available for both. `compi` distributions are self-contained and do not require any dependencies. `compi-dk` only requires Docker, which should be available for the `compi-dk build` command to work.

## Build from source

Alternatively, the compi project can be build to obtain the `compi` and `compi-dk` binaries.

To do so, just download or clone this project and run the following command (*Note*: requires Maven 3.x and Java 1.8): `mvn clean package -PcreateInstaller`

If the build succeeds, then:
- The `compi` and `compi-dk` builds will be available at `compi/cli/target/dist/` and `compi/dk/target/dist/`, respectively. Java is required to run these binaries. The `compi` also requires `envsubst` to be available at runtime and `compi-dk` requires Docker, which should be available for the *compi-dk build* command to work.
-  the `compi` and `compi-dk` Linux 64-bit builds will be available at `compi/cli/target/installer/` and `compi/dk/target/installer/`, respectively.

## Citing

Please, cite the following publication if you use Compi:
- H. López-Fernández; O. Graña-Castro; A. Nogueira-Rodríguez; M. Reboiro-Jato; D. Glez-Peña (2021) **Compi: a Framework for Portable and Reproducible Pipelines**. *PeerJ Computer Science*. Volume 7: e593. ISSN: 2376-5992 [![DOI](https://img.shields.io/badge/DOI-10.7717%2Fpeerj--cs.593-blue)](https://doi.org/10.7717/peerj-cs.593)
