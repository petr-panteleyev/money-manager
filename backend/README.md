# Money Manager Backend

## Environment

| Property          | Description                 | Example   |
|-------------------|-----------------------------|-----------|
| DATABASE_URL      | Database connection URL | jdbc:postgresql://localhost:5432/postgres |
| DATABASE_SCHEMA| Database schema||
| DATABASE_USER     | Database user name          ||
| DATABASE_PASSWORD | Database password           ||

## Build

Make sure ```JAVA_HOME``` points to JDK 19.

```shell script
mvn clean install
```

## Binary Package

To build binary package execute the following command:

```shell script
mvn -pl backend jpackage:jpackage@linux
```

Application image will be found in ```backend/target/dist``` directory.

## Run

### JAR File

```shell
DATABASE_URL=<database url> \
  DATABASE_SCHEMA=<schema> \
  DATABASE_USER=<user> \
  DATABASE_PASSWORD=<password> \
  java -jar backend/target/jar/money-manager-backend-<project version>.jar
```
