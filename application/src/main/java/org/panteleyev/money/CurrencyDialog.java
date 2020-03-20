package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.model.Currency;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.Constants.COLON;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;

final class CurrencyDialog extends BaseDialog<Currency> {
    private final TextField nameEdit = new TextField();
    private final TextField descrEdit = new TextField();
    private final TextField rateEdit = new TextField();
    private final ChoiceBox<String> rateDirectionChoice = new ChoiceBox<>();
    private final CheckBox defaultCheck = newCheckBox(RB, "currency.Dialog.Default");
    private final CheckBox showSymbolCheck = new CheckBox();
    private final ComboBox<String> formatSymbolCombo = new ComboBox<>();
    private final ChoiceBox<String> formatSymbolPositionChoice = new ChoiceBox<>();
    private final CheckBox thousandSeparatorCheck = newCheckBox(RB, "currency.Dialog.ShowSeparator");

    CurrencyDialog(Controller owner, Currency currency) {
        super(owner, MainWindowController.CSS_PATH);

        setTitle(RB.getString("Currency"));

        var gridPane = new GridPane();
        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, newLabel(RB, "label.Symbol"), nameEdit);
        gridPane.addRow(index++, newLabel(RB, "Description", COLON), descrEdit);
        gridPane.addRow(index++, newLabel(RB, "Rate", COLON), rateEdit, rateDirectionChoice);

        var hBox = new HBox(showSymbolCheck, formatSymbolCombo, formatSymbolPositionChoice);
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(formatSymbolPositionChoice, new Insets(0.0, 0.0, 0.0, 5.0));
        gridPane.add(hBox, 1, index++);
        gridPane.add(thousandSeparatorCheck, 1, index++);
        gridPane.add(defaultCheck, 1, index);

        getDialogPane().setContent(gridPane);

        nameEdit.setPrefColumnCount(20);
        formatSymbolCombo.setEditable(true);

        rateDirectionChoice.getItems().setAll("/", "*");

        formatSymbolPositionChoice.getItems().setAll(
            RB.getString("currency.Dialog.Before"),
            RB.getString("currency.Dialog.After"));

        formatSymbolCombo.getItems().setAll(cache().getCurrencies().stream()
            .map(Currency::formatSymbol)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet()));

        if (currency == null) {
            rateDirectionChoice.getSelectionModel().select(0);
            formatSymbolPositionChoice.getSelectionModel().select(0);
            rateEdit.setText("1");
        } else {
            nameEdit.setText(currency.symbol());
            descrEdit.setText(currency.description());
            rateEdit.setText(currency.rate().toString());
            defaultCheck.setSelected(currency.def());
            rateDirectionChoice.getSelectionModel().select(currency.direction());
            showSymbolCheck.setSelected(currency.showFormatSymbol());
            formatSymbolCombo.getSelectionModel().select(currency.formatSymbol());
            formatSymbolPositionChoice.getSelectionModel().select(currency.formatSymbolPosition());
            thousandSeparatorCheck.setSelected(currency.useThousandSeparator());
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                long now = System.currentTimeMillis();

                var builder = new Currency.Builder(currency)
                    .symbol(nameEdit.getText())
                    .description(descrEdit.getText())
                    .formatSymbol(formatSymbolCombo.getSelectionModel().getSelectedItem())
                    .formatSymbolPosition(formatSymbolPositionChoice.getSelectionModel().getSelectedIndex())
                    .showFormatSymbol(showSymbolCheck.isSelected())
                    .def(defaultCheck.isSelected())
                    .rate(new BigDecimal(rateEdit.getText()))
                    .direction(rateDirectionChoice.getSelectionModel().getSelectedIndex())
                    .useThousandSeparator(thousandSeparatorCheck.isSelected())
                    .modified(now);

                if (currency == null) {
                    builder.guid(UUID.randomUUID())
                        .created(now);
                }

                return builder.build();
            } else {
                return null;
            }
        });

        createDefaultButtons(RB);

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty()));

        validation.registerValidator(rateEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    TextField getDescrEdit() {
        return descrEdit;
    }

    TextField getRateEdit() {
        return rateEdit;
    }

    CheckBox getDefaultCheck() {
        return defaultCheck;
    }

    CheckBox getThousandSeparatorCheck() {
        return thousandSeparatorCheck;
    }
}
