# Import Strategy

Money Manager provides the choice from the following list of import
strategies:

* Full dump import
* Import from related database
* Import from unrelated database

## Full Dump Import

This strategy is used to complete rewrite of the target database.
All data is lost and replaced by records from the file.

Import steps:

1. Truncate all target tables
1. Create all records as is in proper order

## Partial Import

This strategy merges import file records with existing records in the
target database using uuid as a means to find duplicates.