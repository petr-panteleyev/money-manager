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

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyDAO;

public class CurrencyDialog extends BaseDialog<Currency.Builder> implements Initializable {
    private static final String FXML = "/org/panteleyev/money/CurrencyDialog.fxml";

    @FXML private TextField             nameEdit;
    @FXML private TextField             descrEdit;
    @FXML private TextField             rateEdit;
    @FXML private ChoiceBox<String>     rateDirectionChoice;
    @FXML private CheckBox              defaultCheck;
    @FXML private CheckBox              showSymbolCheck;
    @FXML private ComboBox<String>      formatSymbolCombo;
    @FXML private ChoiceBox<String>     formatSymbolPositionChoice;
    @FXML private CheckBox              thousandSeparatorCheck;

    private final Currency              currency;

    public CurrencyDialog(Currency currency) {
        super(FXML, MainWindowController.UI_BUNDLE_PATH);

        this.currency = currency;
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        setTitle(rb.getString("currency.Dialog.Title"));

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

        createDefaultButtons();

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(rateEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }
}
