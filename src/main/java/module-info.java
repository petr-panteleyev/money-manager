open module org.panteleyev.money {
    requires java.base;
    requires java.prefs;
    requires java.sql;
    requires java.naming;
    requires java.net.http;

    requires jdk.crypto.cryptoki;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.web;
    requires javafx.swing;          // for tests

    requires org.controlsfx.controls;

    requires org.panteleyev.persistence;
    requires org.panteleyev.commons.ssh;
    requires org.panteleyev.commons.database;
    requires org.panteleyev.commons.fx;

    // Automatic modules
    requires mysql.connector.java;
    requires org.jsoup;
    requires gson;
    requires jsch;
}
