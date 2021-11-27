.label {
    -fx-font-family: "${controlsFontFamily}";
    -fx-font-size: ${controlsFontSize};
}

.button {
    -fx-font-family: "${controlsFontFamily}";
    -fx-font-size: ${controlsFontSize};
}

.text-field {
    -fx-font-family: "${controlsFontFamily}";
    -fx-font-size: ${controlsFontSize};
}

.list-cell {
    -fx-font-family: "${controlsFontFamily}";
    -fx-font-size: ${controlsFontSize};
}

.check-box {
    -fx-font-family: "${controlsFontFamily}";
    -fx-font-size: ${controlsFontSize};
}

.menu {
    -fx-font-family: "${menuFontFamily}";
    -fx-font-size: ${menuFontSize};
}

.menu .label {
    -fx-font-family: "${menuFontFamily}";
    -fx-font-size: ${menuFontSize};
}

.table-cell {
    -fx-font-family: "${tableCellFontFamily}";
    -fx-font-size: ${tableCellFontSize};
}

.groupCell {
    -fx-font-weight: bold;
}

.expired {
    -fx-text-fill: red;
}

.table-row-cell:selected .expired {
    -fx-text-fill: -fx-selection-bar-text;
}

.amount-debit {
    -fx-text-fill: ${debitColor};
}

.table-row-cell:selected .amount-debit {
    -fx-text-fill: -fx-selection-bar-text;
}

.amount-credit {
    -fx-text-fill: ${creditColor};
}

.table-row-cell:selected .amount-credit {
    -fx-text-fill: -fx-selection-bar-text;
}

.amount-transfer {
    -fx-text-fill: ${transferColor};
}

.table-row-cell:selected .amount-transfer {
    -fx-text-fill: -fx-selection-bar-text;
}

.rateLabel {
    -fx-font-size: 11;
    -fx-text-fill: #051aff;
}

.subLabel {
    -fx-font-size: 11;
}

.boldText {
    -fx-font-weight: bold;
}

.gridPane {
    -fx-hgap: 5;
    -fx-vgap: 5;
}

.statementMissing {
    -fx-control-inner-background: ${statementMissingColor};
    -fx-control-inner-background-alt: ${statementMissingColor};
}

.statementChecked {
    -fx-control-inner-background: ${statementCheckedColor};
    -fx-control-inner-background-alt: ${statementCheckedColor};
}

.statementUnchecked {
    -fx-control-inner-background: ${statementUncheckedColor};
    -fx-control-inner-background-alt: ${statementUncheckedColor};
}

.iconLabel {
    -fx-font-size: 10;
}

.selectedIconCell {
    -fx-background: -fx-accent;
    -fx-background-color: -fx-focus-color, -fx-cell-focus-inner-border, -fx-selection-bar;
    -fx-background-insets: 0, 1, 2;
    -fx-text-fill: -fx-selection-bar-text;
}
