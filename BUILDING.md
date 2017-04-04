# Building Money Manager

## Prerequisites

### Source Code

The following repositories must be cloned to build Money Manager:

1. git clone https://github.com/petr-panteleyev/java-utilities.git utilities
2. git clone https://github.com/petr-panteleyev/java-persistence.git persistence
3. git clone https://github.com/petr-panteleyev/java-money.git money

Persistence is available from Maven Central which means it should be cloned only if full sources are required for IDE project.

### JAR Signing configuration
The following properties must be set in `settings.xml`:

```
<keystore.path>/path/to/keystore</keystore.path>
<keystore.alias>keystore-alias</keystore.alias>
<keystore.password>keystore-password</keystore.password>
```

## Building Dependencies

```
cd <utilities>
mvn install -f shared.xml
mvn install

(optionally)
cd <persistence>
mvn install
```

## Building JAR File

```
cd <money>
mvn package
```

## Building Signed JAR File

```
cd <money>
mvn package -P sign
```

## Building Standalone JAR
Standalone (fat) JAR is always signed.

```
cd <money>
mvn clean
mvn package -P fatjar
```

## Building Native Packages

```
cd <money>
mvn clean
mvn package -P fatjar
mvn exec:exec@<native-dist>
```

Where &lt;native-dist> depends on native OS and packaging.

`dist-mac` produces DMG file. Its content can be copied to the Applications folder as is.

`dist-win` produces EXE file with a simple installer. This option requires additional software. Please refer to
[javapackager](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/javapackager.html) documentation for details.

`dist-rpm` produces RPM file. This option was tested on OpenSUSE Leap 42.2.

Resulting package can be found in `<money>/target/dists/bundles`.
