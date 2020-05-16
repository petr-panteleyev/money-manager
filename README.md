# Money Manager

Personal finance manager. Work in progress.

## Build and Run

JDK 14 is required to build and run the application.

### Build

Make sure Maven toolchain configuration ```toolchain.xml``` contains the following
definition:
```xml
<toolchain>
    <type>jdk</type>
    <provides>
        <version>14</version>
    </provides>
    <configuration>
        <jdkHome>/path/to/jdk-14</jdkHome>
    </configuration>
</toolchain>
```
Execute the following:
```shell script
$ mvn clean package
```

Application JAR and all dependencies will be placed in ```target/jmods```.

### Run

```shell script
$ mvn javafx:run
```
To open specific profile add ```-Dmoney.profile=<profile>``` to the command line.

### Binary Packages

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