open module org.panteleyev.money {
    requires java.prefs;
    requires java.sql;
    requires java.naming;
    requires java.xml.bind;

    requires javafx.base;
    requires javafx.controls;
    requires javafx.swing;          // for tests

    requires org.panteleyev.persistence;
    requires org.panteleyev.utilities;

    requires mysql.connector.java;
    requires commons.csv;
    requires controlsfx;
    requires org.jsoup;
}