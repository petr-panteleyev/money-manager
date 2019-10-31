module org.panteleyev.money.persistence {
    exports org.panteleyev.money.persistence;
    exports org.panteleyev.money.xml;

    requires org.panteleyev.persistence;
    requires org.panteleyev.money.model;

    requires java.xml;
    requires java.sql;
    requires javafx.base;

    requires mysql.connector.java;
}