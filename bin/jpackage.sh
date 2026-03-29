#!/bin/sh

# Copyright © 2026 Petr Panteleyev
# SPDX-License-Identifier: BSD-2-Clause
rm -rf ./desktop/app/target/dist
mvn -pl desktop/app jpackage:jpackage
