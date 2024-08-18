/*
 Copyright © 2017-2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
open module org.panteleyev.money {
    requires java.sql;
    requires java.naming;
    requires java.net.http;
    requires java.logging;

    requires jdk.crypto.cryptoki;

    requires org.panteleyev.fx;
    requires org.panteleyev.ofx;
    requires org.panteleyev.moex;
    requires org.panteleyev.commons;
    requires org.panteleyev.freedesktop;
    requires org.panteleyev.money.model;
    requires org.panteleyev.money.desktop.commons;
    requires org.panteleyev.money.desktop.statements;
    requires org.panteleyev.money.desktop.export;
    requires org.panteleyev.money.desktop.persistence;

    requires org.controlsfx.controls;
    requires org.postgresql.jdbc;

    requires org.jsoup;
    requires freemarker;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    requires org.apache.commons.lang3;
}
