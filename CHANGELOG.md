# Change Log

## [18.2.0] - 2018-02-25

- Parser for Sberbank credit and debit cards statements (HTML)
- Adding new transaction from statement view
- Basic pie charts

## [18.1.0] - 2018-01-14

- Java 9
- Support for Raiffeisen Bank (Russia) OFX statement

### Fixed

- NPE while reloading statement

## [2.2.0] - 2017-10-15

- Connection profiles replaced manual connection and single option
- Parser for Raiffeisen Bank (Russia) credit card statement

## [2.1.0] - 2017-08-19

- Export/import of the entire database and selected transactions
- Import utility

## [2.0.0] - 2017-06-04

- Migrated to MySQL
- Enum fields replaced by integer id
- Database schema added to docs

## [1.1.1] - 2017-05-27

- No autocompletion menu in case of a single match
- Option for autocomplete prefix length
- Options dialog supports autocomplete prefix length
- About dialog updated with build timestamp

## [1.1.0] - 2017-05-14

- Autocomplete works by "contains" instead of "starts with"
- Autocomplete requires 3 letters to work instead of 2
- FXML replaced by Java implementation for faster UI startup
- Application jar is not signed anymore, check PGP instead
- Performance and memory optimizations

## [1.0.0] - 2017-04-04

- Empty transaction type is set to "undefined"
- New transaction table colors
- Auto completion requires 2 letters to show popup
- Transaction table scrolls to the first or last day depending on sort order
- Fixed: random exception while reloading table with selected row
- Performance optimizations to speed up database preload
- GC options for native bundles
- Additional info in BUILDING.md

### Fixed

- Incorrect day value in transaction editor
- FX thread related exceptions
- Rare exception related to transaction auto-fill
- Incorrect colors for transaction sum
- Inaccurate transaction editor layout
- Empty rate prevented transaction submission in some cases

## [1.0.0-beta] - 2017-03-29

- System menu for all windows on OS X
- --file creates new file if necessary
- --file does not overwrite default file setting
- Several bug fixes in UI
- Localization corrections
- About dialog
- Split divider position is closer to bottom
- Transaction details auto-fill
- Main window dimensions are remembered
- Date field gets focus in transaction editor

## [1.0.0-alpha] - 2017-03-19

- Major functionality implemented: accounts, transactions, etc.
- Localization
- Native bundles: OS X, Windows, RPM

## [0.3.0] - 2017-02-23

- Filter for accounts and transactions in "Accounts" tab
- Filter for transactions in "Transactions" tab

## [0.2.0] - 2017-02-19

- Schema updated to use enum values instead of id
- Updated colors for transaction table
- All styles moved to main.css resource
- Main menu localization

## [0.1.0] - 2017-02-12

- Accounts can be created from accounts tree
- Transactions table added to accounts tab
- Localization

## [0.0.1] - 2017-01-08

- Database schema defined
- UI for account tree
- UI for transactions
- Transaction editor
- Basic query
- Partial localization