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
mvn package -P fatjar
```
