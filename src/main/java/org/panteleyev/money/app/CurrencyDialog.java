/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

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
import org.panteleyev.money.model.Currency;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import static javafx.geometry.Pos.CENTER_LEFT;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.grid.GridBuilder.SKIP;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEFAULT_CURRENCY;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_SHOW_THOUSAND_SEPARATOR;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_AFTER;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_BEFORE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_RATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SYMBOL;
import static org.panteleyev.money.persistence.DataCache.cache;

final class CurrencyDialog extends BaseDialog<Currency> {
    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final TextField descrEdit = new TextField();
    private final TextField rateEdit = new TextField();
    private final ChoiceBox<String> rateDirectionChoice = new ChoiceBox<>();
    private final CheckBox defaultCheck = newCheckBox(UI, I18N_MISC_DEFAULT_CURRENCY);
    private final CheckBox showSymbolCheck = new CheckBox();
    private final ComboBox<String> formatSymbolCombo = new ComboBox<>();
    private final ChoiceBox<String> formatSymbolPositionChoice = new ChoiceBox<>();
    private final CheckBox thousandSeparatorCheck = newCheckBox(UI, I18N_MISC_SHOW_THOUSAND_SEPARATOR);

    CurrencyDialog(Controller owner, URL css, Currency currency) {
        super(owner, css);

        setTitle(fxString(UI, I18N_WORD_CURRENCY));

        getDialogPane().setContent(
            gridPane(
                List.of(
                    gridRow(label(fxString(UI, I18N_WORD_SYMBOL, COLON)), nameEdit),
                    gridRow(label(fxString(UI, I18N_WORD_DESCRIPTION, COLON)), descrEdit),
                    gridRow(label(fxString(UI, I18N_WORD_RATE, COLON)), rateEdit, rateDirectionChoice),
                    gridRow(SKIP, hBox(List.of(showSymbolCheck, formatSymbolCombo, formatSymbolPositionChoice), hBox -> {
                        hBox.setAlignment(CENTER_LEFT);
                        HBox.setMargin(formatSymbolPositionChoice, new Insets(0.0, 0.0, 0.0, 5.0));
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
            UI.getString(I18N_WORD_BEFORE),
            UI.getString(I18N_WORD_AFTER));

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
