open module org.panteleyev.money {
    requires java.base;
    requires java.prefs;
    requires java.sql;
    requires java.naming;
    requires java.net.http;

    requires jdk.crypto.cryptoki;

    requires javafx.web;
    requires javafx.swing;          // for tests

    requires org.panteleyev.fx;

    requires org.panteleyev.money.model;
    requires org.panteleyev.ofx;
    requires org.panteleyev.money.persistence;
    requires org.panteleyev.money.statements;
    requires org.panteleyev.money.ymoney;

    requires com.google.gson;
    requires freemarker;

    requires mysql.connector.java;
}
