/*
 Copyright © 2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
module org.panteleyev.money.desktop.commons {
    exports org.panteleyev.money.desktop.commons;
    exports org.panteleyev.money.desktop.commons.xml;

    requires java.xml;

    requires javafx.base;

    requires org.panteleyev.money.model;
}