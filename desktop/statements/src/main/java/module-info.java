/*
 Copyright © 2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
module org.panteleyev.money.desktop.statements {
    exports org.panteleyev.money.statements;

    requires java.logging;

    requires org.jsoup;

    requires javafx.base;

    requires org.panteleyev.money.model;
    requires org.panteleyev.ofx;
    requires org.panteleyev.money.desktop.commons;
}