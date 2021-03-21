# Money Manager

[![BSD-2 license](https://img.shields.io/badge/License-BSD--2-informational.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-16-orange?logo=java)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![JavaFX](https://img.shields.io/badge/JavaFX-16-orange?logo=java)](https://openjfx.io/)

Personal finance manager. Work in progress.

## Build

Make sure ```JAVA_HOME``` is set to JDK 16.

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
$ mvn clean package jpackage:jpackage@mac
  or
$ mvn clean package jpackage:jpackage@win
```

Installation package will be found in ```target/dist``` directory.

## Support

There is no support for this application.
