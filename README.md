# Money Manager

Personal finance manager. Work in progress.

## Build and Run

JDK 14 is required to build and run the application.

### Build

Execute the following:
```shell script
$ ./gradlew clean build
```

Application JAR and all dependencies will be placed in ```build/jmods```.

### Run

```shell script
$ ./gradlew run
```
To open specific profile add ```-Dmoney.profile=<profile>``` to the command line.

### Binary Packages

To build binary installers perform the following steps:
* On Microsoft Windows: install [WiX Toolset](https://wixtoolset.org/releases/), add its binary directory to ```PATH``` 
environment variable
* Execute the following commands:
```shell script
$ ./gradlew clean jpackage
```

Installation package will be found in ```build/dist``` directory.

## Support

There is no support for this application.