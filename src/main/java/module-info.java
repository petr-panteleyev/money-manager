open module org.panteleyev.money {
    requires java.base;
    requires java.prefs;
    requires java.sql;
    requires java.naming;

    requires jdk.crypto.cryptoki;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.web;
    requires javafx.swing;          // for tests
    requires jdk.incubator.httpclient;

    requires org.panteleyev.persistence;
    requires org.panteleyev.utilities;
    requires org.panteleyev.crypto;

    // Automatic modules
    requires mysql.connector.java;
    requires commons.csv;
    requires controlsfx;
    requires jsoup;
    requires gson;
    requires jsch;
}