/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.currency;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.MainWindowController;
import org.panteleyev.money.model.Currency;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static javafx.geometry.Pos.CENTER_LEFT;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxUtils.SKIP;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.GRID_PANE;

final class CurrencyDialog extends BaseDialog<Currency> {
    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final TextField descrEdit = new TextField();
    private final TextField rateEdit = new TextField();
    private final ChoiceBox<String> rateDirectionChoice = new ChoiceBox<>();
    private final CheckBox defaultCheck = new CheckBox("Валюта по умолчанию");
    private final CheckBox showSymbolCheck = new CheckBox();
    private final ComboBox<String> formatSymbolCombo = new ComboBox<>();
    private final ChoiceBox<String> formatSymbolPositionChoice = new ChoiceBox<>();
    private final CheckBox thousandSeparatorCheck = new CheckBox("Показывать разделитель тысяч");

    CurrencyDialog(Controller owner, String css, Currency currency) {
        super(owner, css);

        setTitle("Валюта");

        getDialogPane().setContent(
                gridPane(
                        List.of(
                                gridRow(label("Символ:"), nameEdit),
                                gridRow(label("Описание:"), descrEdit),
                                gridRow(label("Курс:"), rateEdit, rateDirectionChoice),
                                gridRow(SKIP, hBox(List.of(showSymbolCheck, formatSymbolCombo,
                                        formatSymbolPositionChoice), hBox -> {
                                    hBox.setAlignment(CENTER_LEFT);
                                    HBox.setMargin(formatSymbolPositionChoice, new Insets(0.0, 0.0, 0.0, BIG_SPACING));
                                })),
                                gridRow(SKIP, thousandSeparatorCheck),
                                gridRow(SKIP, defaultCheck)

                        ), b -> b.withStyle(GRID_PANE)
                )
        );

        nameEdit.setPrefColumnCount(20);
        formatSymbolCombo.setEditable(true);

        rateDirectionChoice.getItems().setAll("/", "*");

        formatSymbolPositionChoice.getItems().setAll(
                "Перед",
                "После");

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
            if (b != ButtonType.OK) {
                return null;
            }

            var now = System.currentTimeMillis();

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
                builder.uuid(UUID.randomUUID())
                        .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());

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
