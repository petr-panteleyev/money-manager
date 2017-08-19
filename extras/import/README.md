# Import Utility

This utility performs conversion from external financial data file to
Money Manager XML. This XML can be imported via Tools->Import...
menu item.

The only supported input file format is full XML dump produced by
iCash 7.5.


## Usage

```java -jar money-manager-import-1.0.0.jar <source.xml> [<target.xml>]```

If target file is not specified conversion result is printed to stdout.