/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
open module org.panteleyev.money {
    requires java.base;
    requires java.sql;
    requires java.naming;
    requires java.net.http;
    requires java.logging;

    requires jdk.crypto.cryptoki;

    requires org.panteleyev.money.model;
    requires org.panteleyev.fx;
    requires org.panteleyev.ofx;
    requires org.panteleyev.freedesktop;

    requires org.controlsfx.controls;

    requires freemarker;
    requires org.jsoup;

    requires mysql.connector.java;
    requires liquibase.core;
}
