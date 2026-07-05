// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
module org.panteleyev.money.dto {
    exports org.panteleyev.money.dto;

    requires java.net.http;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;

    requires transitive jakarta.annotation;
    requires transitive org.jspecify;
}