# Money Manager

Программа для учета расходов и сверки выписок.

## Как собрать

Для сборки проекта требуется JDK 24+.

```shell script
export JAVA_HOME=/path/to/jdk24
./mvnw clean install
```

## Как запустить

```shell script
./mvnw -pl desktop/app exec:exec
```

Чтобы открыть конкретный профиль соединения добавьте ```-Dmoney.profile=<profile>``` к командной строке.

## Установка

```shell script
./mvnw -pl desktop/app jpackage:jpackage
```

Пакет для установки будет находиться в каталоге ```desktop/app/target/dist```.

## Поддержка

Поддержка данного приложения не осуществляется.
