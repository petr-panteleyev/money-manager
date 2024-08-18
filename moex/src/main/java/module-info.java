module org.panteleyev.moex {
    requires java.net.http;
    requires java.xml;

    requires org.panteleyev.commons;

    exports org.panteleyev.moex;
    exports org.panteleyev.moex.model;
}