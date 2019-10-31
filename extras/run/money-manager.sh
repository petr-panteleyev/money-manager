#!/bin/sh

java --module-path `dirname $0` --add-exports javafx.base/com.sun.javafx.event=controlsfx --add-reads org.panteleyev.persistence=org.panteleyev.money.model -Dfile.encoding=UTF-8 -m org.panteleyev.money/org.panteleyev.money.MoneyApplication
