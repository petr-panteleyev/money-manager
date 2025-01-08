# Money Manager

![JDK](docs/badges/java.svg)
[![License](docs/badges/license.svg)](LICENSE)

Программа для учета расходов и сверки выписок.

## Как собрать

Убедитесь, что переменная окружения ```JAVA_HOME``` указывает на JDK 23.

```shell script
./mvnw clean install
```

## Как запустить

```shell script
./mvnw -pl desktop/app exec:exec
```

Чтобы открыть конкретный профиль соединения добавьте ```-Dmoney.profile=<profile>``` к командной строке.

## Установка

Чтобы собрать пакет для установки приложения выполните следующие шаги:
* На Microsoft Windows: установите [WiX Toolset](https://wixtoolset.org/releases/), добавьте каталог с утилитами в переменную окружения ```PATH```
* Выполните команду:

```shell script
./mvnw -pl desktop/app jpackage:jpackage
```

Пакет для установки будет находиться в каталоге ```desktop/app/target/dist```.

## Поддержка

Поддержка данного приложения не осуществляется.
