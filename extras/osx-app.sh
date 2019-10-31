#!/bin/sh

rm -rf target/dist

$JPACKAGE_HOME/bin/jpackage \
    --module org.panteleyev.money/org.panteleyev.money.MoneyApplication \
    --runtime-image $JAVA_HOME \
    --verbose \
    --dest target/dist \
    -p target/jmods \
    --java-options "--add-exports javafx.base/com.sun.javafx.event=org.controlsfx.controls --add-reads org.panteleyev.persistence=org.panteleyev.money.model" \
    --icon icons/icons.icns \
    --name "Money Manager"
