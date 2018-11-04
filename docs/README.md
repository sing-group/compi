# Compi docs

## Building the docs

The documentation can be build using maven or `sphinx` directly. Before building the documentation, check the `source/conf.py` file to make sure that the compi version is right.

### Building the docs with maven

Simply run `mvn package -PgenerateDocs`in the parent directory. Documentation will be generated in `./docs/build/html` directory.

### Building the docs with sphinx

You need `sphinx` installed to generate the documentation. You also need to install the ReadTheDocs theme:

	pip install sphinx_rtd_theme

Placed in this directory, run `make html`. Documentation will be generated in `./build/html` directory.

If you want that the documentation builds automatically when you change any file, use inotify:

	inotifywait -e close_write -q -m -r source |while read events; do rm -rf build/html/*; make html; done

And if you want to see the browser reloading after each change in the documentation, serve it locally with (for example with npm serve). The documentation includes
a JavaScript file based on live.js, which reloads the page when the source files change (polling the server with HEAD and only when it is served in localhost):

	serve build/html

## Writing the documentation

The documentation source files are located at the `./source` directory.

Documentation is written using [reStructuredText](http://docutils.sourceforge.net/rst.html). In the following links you can find information about the syntax:
- http://docutils.sourceforge.net/docs/user/rst/quickref.html
- http://docutils.sourceforge.net/docs/ref/rst/restructuredtext.html
