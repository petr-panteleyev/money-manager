// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
module money.manager.client {
    requires java.net.http;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.datatype.jdk8;

    requires jakarta.annotation;
    requires org.jspecify;

    requires transitive org.panteleyev.functional;
    requires org.panteleyev.money.dto;

    exports org.panteleyev.money.client;
}
