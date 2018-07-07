# Money Manager

Personal finance manager. Work in progress.

## Running with Java 10

The following options must be added to java command line:

* --add-exports javafx.base/com.sun.javafx.event=controlsfx
* --add-reads org.panteleyev.persistence=org.panteleyev.money

**Example**:

java --module-path c:\apps\money-manager --add-exports javafx.base/com.sun.javafx.event=controlsfx \
  --add-reads org.panteleyev.persistence=org.panteleyev.money -Dfile.encoding=UTF-8 \
  -m org.panteleyev.money/org.panteleyev.money.MoneyApplication