# Money Manager

[![BSD-2 license](https://img.shields.io/badge/License-BSD--2-informational.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-18-orange?logo=java)](https://jdk.java.net/18/)
[![JavaFX](https://img.shields.io/badge/JavaFX-18-orange?logo=java)](https://openjfx.io/)

Personal finance manager. Work in progress.

## Build

Make sure ```JAVA_HOME``` is set to JDK 18.

```shell script
mvn clean package
```

Application JAR and all dependencies will be placed in ```target/jmods```.

## Run

```shell script
mvn javafx:run
```

To open specific profile add ```-Dprofile=<profile>``` to the command line.

## Binary Packages

To build binary installers perform the following steps:
* On Microsoft Windows: install [WiX Toolset](https://wixtoolset.org/releases/), add its binary directory to ```PATH``` 
environment variable
* Execute one of the following commands:

```shell script
mvn clean package jpackage:jpackage@mac
```

```shell script
mvn clean package jpackage:jpackage@win
```

```shell script
mvn clean package jpackage:jpackage@linux
```

Installation package will be found in ```target/dist``` directory.

## Support

There is no support for this application.
