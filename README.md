# Money Manager

Программа для учета расходов и сверки выписок.

## Как собрать

Для сборки проекта требуется JDK 25+.

```shell script
export JAVA_HOME=/path/to/jdk25
mvn clean install
```

## Как запустить

```shell script
mvn -pl desktop/app exec:exec@run
```

Чтобы открыть конкретный профиль соединения добавьте ```-Dmoney.profile=<profile>``` к командной строке.

## Установка

```shell script
mvn -pl desktop/app jpackage:jpackage
```

Пакет для установки будет находиться в каталоге ```desktop/app/target/dist```.

## Поддержка

Поддержка данного приложения не осуществляется.

## Использование AI

[GigaCode](https://gitverse.ru/features/gigacode/) был использован для генерации некоторых тестов.
