#!/bin/sh

rm -rf ./desktop/app/target/dist
./mvnw -pl desktop/app jpackage:jpackage
