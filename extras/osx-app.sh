#!/bin/sh

rm -rf target/dist

$JPACKAGE_HOME/bin/jpackage create-app-image \
    --module org.panteleyev.money/org.panteleyev.money.MoneyApplication \
    --runtime-image $JAVA_HOME \
    --verbose \
    --output target/dist \
    -p target/jmods \
    --java-options "--add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls --add-reads org.panteleyev.persistence=org.panteleyev.money" \
    --icon icons/icons.icns \
    --name "Money Manager"
