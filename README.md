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
- *Compi GUI*: a universal graphical user interface for running Compi pipelines (https://github.com/sing-group/compi-gui).

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

# AI-assisted pipeline engineering

The [compi-pipeline-engineering](https://github.com/sing-group/compi-pipeline-engineering) repository provides a `SKILL.md` file for AI coding agents (such as [OpenCode](https://opencode.ai)) to diagnose, refactor, and verify Compi pipelines according to the SOLID design principles, robustness checks, and reproducibility best practices presented in:

- D. Pérez-Rodríguez; A. Nogueira-Rodríguez; J. Vieira; C. Vieira; D. Glez-Peña; H. López-Fernández (2026) **Software Engineering for Reproducible Pipeline Development in Bioinformatics**. *Frontiers in Bioinformatics*. Volume 6 (2026), Article ID 1824590. ISSN: 2673-7647 [![DOI](https://img.shields.io/badge/DOI-10.3389%2Ffbinf.2026.1824590-blue)](https://doi.org/10.3389/fbinf.2026.1824590)

You can download the skill version compatible with your installed `compi-dk` directly using:

```bash
compi-dk get-compi-skills -p .opencode/skills/compi-pipeline-engineering
```

## Citing

Please, cite the following publications if you use Compi:
- H. López-Fernández; O. Graña-Castro; A. Nogueira-Rodríguez; M. Reboiro-Jato; D. Glez-Peña (2021) **Compi: a Framework for Portable and Reproducible Pipelines**. *PeerJ Computer Science*. Volume 7: e593. ISSN: 2376-5992 [![DOI](https://img.shields.io/badge/DOI-10.7717%2Fpeerj--cs.593-blue)](https://doi.org/10.7717/peerj-cs.593)
- A. Nogueira-Rodríguez; D. Pérez-Rodríguez; F. González-Doval; D. Glez-Peña; H. López-Fernández (2026) **Enhancing the Compi Ecosystem with Graphical User Interfaces and Reusable Modules**. *20th International Conference on Practical Applications of Computational Biology & Bioinformatics (PACBB 2026)*. Naples, Italy, 21 October
