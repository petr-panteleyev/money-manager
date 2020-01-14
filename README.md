# Money Manager

Personal finance manager. Work in progress.

## Build and Run

### Build

Make sure Maven toolchain configuration ```toolchain.xml``` contains the following
definition:
```xml
<toolchain>
    <type>jdk</type>
    <provides>
        <version>13</version>
    </provides>
    <configuration>
        <jdkHome>/path/to/jdk-13</jdkHome>
    </configuration>
</toolchain>
```
Execute the following:
```shell script
$ mvn clean package
```

Application JAR and all dependencies will be placed in ```target/jmods```.

### Run

JDK-13 is required to run the application.

```shell script
$ java --module-path target/jmods --add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls \
  --add-reads org.panteleyev.mysqlapi=org.panteleyev.money.model -Dfile.encoding=UTF-8 \
  -m org.panteleyev.money/org.panteleyev.money.MoneyApplication
```

### Binary Packages

To build binary installers perform the following steps:
* Install [JDK-14 EA](https://jdk.java.net/14/) build
* Set ```JPACKAGE_HOME``` environment variable to the root directory of JDK-14
* On Microsoft Windows: install [WiX Toolset](https://wixtoolset.org/releases/), add its binary directory to ```PATH``` 
environment variable
* Execute the following commands:
```shell script
$ mvn clean package
$ ./extras/osx-app.sh
  or
$ ./extras/win-app.sh
```

Installation packages will be found in ```target/dist``` directory.

## Support

There is no support for this application.