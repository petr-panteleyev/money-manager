/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.CurrencyType;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;

final class SecurityDialog extends BaseDialog<Currency> {
    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final TextField descrEdit = new TextField();
    private final TextField rateEdit = new TextField();
    private final TextField isinEdit = new TextField();
    private final TextField registryEdit = new TextField();

    SecurityDialog(Controller owner, URL css, Currency currency) {
        super(owner, css);

        setTitle("Ценная бумага");

        getDialogPane().setContent(
                gridPane(
                        List.of(
                                gridRow(label("Полное наименование:"), descrEdit),
                                gridRow(label("Аббревиатура:"), nameEdit),
                                gridRow(label("ISIN:"), isinEdit),
                                gridRow(label("Гос. регистрация:"), registryEdit),
                                gridRow(label("Котировка:"), rateEdit)
                        ), b -> b.withStyle(GRID_PANE)
                )
        );

        nameEdit.setPrefColumnCount(20);

        if (currency == null) {
            rateEdit.setText("0");
        } else {
            nameEdit.setText(currency.symbol());
            descrEdit.setText(currency.description());
            rateEdit.setText(currency.rate().toString());
            isinEdit.setText(currency.isin());
            registryEdit.setText(currency.registry());
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            long now = System.currentTimeMillis();

            var builder = new Currency.Builder(currency)
                    .type(CurrencyType.SECURITY)
                    .symbol(nameEdit.getText())
                    .description(descrEdit.getText())
                    .isin(isinEdit.getText())
                    .registry(registryEdit.getText())
                    .rate(new BigDecimal(rateEdit.getText()))
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
}
