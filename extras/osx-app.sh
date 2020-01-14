#!/bin/sh

rm -rf target/dist

$JPACKAGE_HOME/bin/jpackage \
    --module org.panteleyev.money/org.panteleyev.money.MoneyApplication \
    --runtime-image $JAVA_HOME \
    --verbose \
    --dest target/dist \
    -p target/jmods \
    --java-options "--add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls --add-reads org.panteleyev.mysqlapi=org.panteleyev.money.model -Dfile.encoding=UTF-8" \
    --icon icons/icons.icns \
    --name "Money Manager" \
    --app-version 20.1.0 \
    --vendor panteleyev.org
