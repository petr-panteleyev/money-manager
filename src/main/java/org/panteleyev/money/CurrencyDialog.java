/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.panteleyev.money;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.utilities.fx.BaseDialog;
import java.math.BigDecimal;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

class CurrencyDialog extends BaseDialog<Currency.Builder> implements Styles {
    private final ResourceBundle    rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final TextField         nameEdit = new TextField();
    private final TextField         descrEdit = new TextField();
    private final TextField         rateEdit = new TextField();
    private final ChoiceBox<String> rateDirectionChoice = new ChoiceBox<>();
    private final CheckBox          defaultCheck = new CheckBox(rb.getString("currency.Dialog.Default"));
    private final CheckBox          showSymbolCheck = new CheckBox();
    private final ComboBox<String>  formatSymbolCombo = new ComboBox<>();
    private final ChoiceBox<String> formatSymbolPositionChoice = new ChoiceBox<>();
    private final CheckBox          thousandSeparatorCheck = new CheckBox(rb.getString("currency.Dialog.ShowSeparator"));

    private final Currency          currency;

    CurrencyDialog(Currency currency) {
        super(MainWindowController.DIALOGS_CSS);

        this.currency = currency;

        initialize();
    }

    private void initialize() {
        setTitle(rb.getString("currency.Dialog.Title"));

        GridPane pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);

        int index = 0;
        pane.addRow(index++, new Label(rb.getString("label.Symbol")), nameEdit);
        pane.addRow(index++, new Label(rb.getString("label.Description")), descrEdit);
        pane.addRow(index++, new Label(rb.getString("label.Rate")), rateEdit, rateDirectionChoice);

        HBox hBox = new HBox(showSymbolCheck, formatSymbolCombo, formatSymbolPositionChoice);
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(formatSymbolPositionChoice, new Insets(0, 0, 0, 5));
        pane.add(hBox, 1, index++);

        pane.add(thousandSeparatorCheck, 1, index++);
        pane.add(defaultCheck, 1, index);

        nameEdit.setPrefColumnCount(20);
        formatSymbolCombo.setEditable(true);

        rateDirectionChoice.getItems().setAll("/", "*");

        formatSymbolPositionChoice.getItems().setAll(
                rb.getString("currency.Dialog.Before"),
                rb.getString("currency.Dialog.After"));

        formatSymbolCombo.getItems().setAll(
                MoneyDAO.getInstance().getCurrencies().stream()
                        .filter(x -> x.getFormatSymbol() != null && !x.getFormatSymbol().isEmpty())
                        .map(Currency::getFormatSymbol)
                        .collect(Collectors.toList())
        );

        if (currency == null) {
            rateDirectionChoice.getSelectionModel().select(0);
            formatSymbolPositionChoice.getSelectionModel().select(0);
            rateEdit.setText("1");
        } else {
            nameEdit.setText(currency.getSymbol());
            descrEdit.setText(currency.getDescription());
            rateEdit.setText(currency.getRate().toString());
            defaultCheck.setSelected(currency.isDef());
            rateDirectionChoice.getSelectionModel().select(currency.getDirection());
            showSymbolCheck.setSelected(currency.isShowFormatSymbol());
            formatSymbolCombo.getSelectionModel().select(currency.getFormatSymbol());
            formatSymbolPositionChoice.getSelectionModel().select(currency.getFormatSymbolPosition());
            thousandSeparatorCheck.setSelected(currency.isUseThousandSeparator());
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                return new Currency.Builder(this.currency)
                    .symbol(nameEdit.getText())
                    .description(descrEdit.getText())
                    .def(defaultCheck.isSelected())
                    .rate(new BigDecimal(rateEdit.getText()))
                    .direction(rateDirectionChoice.getSelectionModel().getSelectedIndex())
                    .formatSymbol(formatSymbolCombo.getSelectionModel().getSelectedItem())
                    .formatSymbolPosition(formatSymbolPositionChoice.getSelectionModel().getSelectedIndex())
                    .showFormatSymbol(showSymbolCheck.isSelected())
                    .useThousandSeparator(thousandSeparatorCheck.isSelected());
            } else {
                return null;
            }
        });

        getDialogPane().setContent(pane);
        createDefaultButtons(rb);

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(rateEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }
}
