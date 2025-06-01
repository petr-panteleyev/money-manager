/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
module org.panteleyev.money.desktop.export {
    exports org.panteleyev.money.desktop.export;

    requires java.logging;

    requires javafx.base;

    requires org.panteleyev.money.model;
    requires org.panteleyev.money.desktop.commons;
    requires org.panteleyev.commons;
    requires java.xml;
}