# Money Manager

Программа для учета расходов и сверки выписок.

## Как собрать

Убедитесь, что переменная окружения ```JAVA_HOME``` указывает на [JDK 22](https://jdk.java.net/22/).

```shell script
mvn clean install
```

## Как запустить

```shell script
mvn -pl desktop javafx:run
```

Чтобы открыть конкретный профиль соединения добавьте ```-Dprofile=<profile>``` к командной строке.

## Установка

Чтобы собрать пакет для установки приложения выполните следующие шаги:
* На Microsoft Windows: установите [WiX Toolset](https://wixtoolset.org/releases/), добавьте каталог с утилитами в переменную окружения ```PATH```
* Выполните команду:

```shell script
mvn -pl desktop jpackage:jpackage
```

Пакет для установки будет находиться в каталоге ```desktop/target/dist```.

## Поддержка

Поддержка данного приложения не осуществляется.
