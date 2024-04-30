/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
open module org.panteleyev.money {
    requires java.base;
    requires java.sql;
    requires java.naming;
    requires java.net.http;
    requires java.logging;

    requires jdk.crypto.cryptoki;

    requires org.panteleyev.fx;
    requires org.panteleyev.ofx;
    requires org.panteleyev.moex;
    requires org.panteleyev.freedesktop;
    requires org.panteleyev.money.model;

    requires org.controlsfx.controls;

    requires freemarker;
    requires org.jsoup;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    requires org.postgresql.jdbc;
    requires liquibase.core;
}
