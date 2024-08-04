open module org.panteleyev.money.desktop.persistence {
    exports org.panteleyev.money.desktop.persistence;

    requires java.naming;
    requires java.sql;

    requires org.postgresql.jdbc;
    requires liquibase.core;
    requires javafx.base;
    requires javafx.controls;

    requires org.panteleyev.money.model;
    requires org.panteleyev.money.desktop.commons;
    requires org.panteleyev.money.desktop.export;
}