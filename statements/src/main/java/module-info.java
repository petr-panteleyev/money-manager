module org.panteleyev.money.statements {
    exports org.panteleyev.money.statements;

    requires java.logging;

    requires org.panteleyev.money.model;
    requires org.panteleyev.money.persistence;
    requires org.panteleyev.ofx;

    requires org.jsoup;
    requires commons.csv;
}