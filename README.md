# Money Manager

[![BSD-2 license](https://img.shields.io/badge/License-BSD--2-informational.svg)](LICENSE)
[![Licence](https://img.shields.io/badge/Java-14-orange?logo=java)](https://www.oracle.com/java/technologies/javase-downloads.html)

Personal finance manager. Work in progress.

## Build

Configure [Maven toolchain](http://maven.apache.org/guides/mini/guide-using-toolchains.html) to provide ```jdk``` version ```14```.

```shell script
$ mvn clean package
```

Application JAR and all dependencies will be placed in ```target/jmods```.

## Run

```shell script
$ mvn javafx:run
```

To open specific profile add ```-Dprofile=<profile>``` to the command line.

## Binary Packages

To build binary installers perform the following steps:
* On Microsoft Windows: install [WiX Toolset](https://wixtoolset.org/releases/), add its binary directory to ```PATH``` 
environment variable
* Execute the following commands:

```shell script
$ mvn clean package
$ mvn jpackage:jpackage@mac
  or
$ mvn jpackage:jpackage@win
```

Installation package will be found in ```target/dist``` directory.

## Support

There is no support for this application.