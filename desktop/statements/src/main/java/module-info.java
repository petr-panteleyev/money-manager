/*
 Copyright © 2024-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
module org.panteleyev.money.desktop.statements {
    exports org.panteleyev.money.statements;

    requires java.logging;

    requires org.jsoup;
    requires org.apache.commons.csv;

    requires javafx.base;

    requires org.panteleyev.money.model;
    requires org.panteleyev.ofx;
    requires org.panteleyev.money.desktop.commons;
}