open module org.panteleyev.money {
    requires java.base;
    requires java.prefs;
    requires java.sql;
    requires java.naming;
    requires java.net.http;
    requires java.logging;

    requires jdk.crypto.cryptoki;

    requires javafx.web;
    requires javafx.swing;          // for tests

    requires org.panteleyev.fx;
    requires org.panteleyev.ofx;
    requires org.panteleyev.mysqlapi;

    requires com.google.gson;
    requires freemarker;
    requires org.jsoup;
    requires commons.csv;

    requires mysql.connector.java;
}
