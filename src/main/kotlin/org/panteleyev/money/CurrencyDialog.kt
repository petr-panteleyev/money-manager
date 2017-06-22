/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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

package org.panteleyev.money

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.utilities.fx.BaseDialog
import java.math.BigDecimal
import java.util.ResourceBundle

class CurrencyDialog(val currency : Currency?) : BaseDialog<Currency>(MainWindowController.DIALOGS_CSS) {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val nameEdit = TextField()
    private val descrEdit = TextField()
    private val rateEdit = TextField()
    private val rateDirectionChoice = ChoiceBox<String>()
    private val defaultCheck = CheckBox(rb.getString("currency.Dialog.Default"))
    private val showSymbolCheck = CheckBox()
    private val formatSymbolCombo = ComboBox<String>()
    private val formatSymbolPositionChoice = ChoiceBox<String>()
    private val thousandSeparatorCheck = CheckBox(rb.getString("currency.Dialog.ShowSeparator"))

    init {
        title = rb.getString("currency.Dialog.Title")

        dialogPane.content = GridPane().apply {
            styleClass.add(Styles.GRID_PANE)

            var index = 0
            addRow(index++, Label(rb.getString("label.Symbol")), nameEdit)
            addRow(index++, Label(rb.getString("label.Description")), descrEdit)
            addRow(index++, Label(rb.getString("label.Rate")), rateEdit, rateDirectionChoice)

            val hBox = HBox(showSymbolCheck, formatSymbolCombo, formatSymbolPositionChoice)
            hBox.alignment = Pos.CENTER_LEFT
            HBox.setMargin(formatSymbolPositionChoice, Insets(0.0, 0.0, 0.0, 5.0))
            add(hBox, 1, index++)

            add(thousandSeparatorCheck, 1, index++)
            add(defaultCheck, 1, index)
        }

        nameEdit.prefColumnCount = 20
        formatSymbolCombo.isEditable = true

        rateDirectionChoice.items.setAll("/", "*")

        formatSymbolPositionChoice.items.setAll(
                rb.getString("currency.Dialog.Before"),
                rb.getString("currency.Dialog.After"))

        formatSymbolCombo.items.setAll(
                MoneyDAO.getCurrencies()
                        .map { it.formatSymbol }
                        .filter { !it.isEmpty() }
        )

        if (currency == null) {
            rateDirectionChoice.selectionModel.select(0)
            formatSymbolPositionChoice.selectionModel.select(0)
            rateEdit.text = "1"
        } else {
            nameEdit.text = currency.symbol
            descrEdit.text = currency.description
            rateEdit.text = currency.rate.toString()
            defaultCheck.isSelected = currency.def
            rateDirectionChoice.selectionModel.select(currency.direction)
            showSymbolCheck.isSelected = currency.showFormatSymbol
            formatSymbolCombo.selectionModel.select(currency.formatSymbol)
            formatSymbolPositionChoice.selectionModel.select(currency.formatSymbolPosition)
            thousandSeparatorCheck.isSelected = currency.useThousandSeparator
        }

        setResultConverter { b: ButtonType ->
            if (b == ButtonType.OK) {
                return@setResultConverter Currency(currency?.id?:0,
                        symbol = nameEdit.text,
                        description = descrEdit.text,
                        def = defaultCheck.isSelected,
                        rate = BigDecimal(rateEdit.text),
                        direction = rateDirectionChoice.selectionModel.selectedIndex,
                        formatSymbol = formatSymbolCombo.selectionModel.selectedItem,
                        formatSymbolPosition = formatSymbolPositionChoice.selectionModel.selectedIndex,
                        showFormatSymbol = showSymbolCheck.isSelected,
                        useThousandSeparator = thousandSeparatorCheck.isSelected)
            } else {
                return@setResultConverter null
            }
        }

        createDefaultButtons(rb)

        Platform.runLater { createValidationSupport() }
    }

    private fun createValidationSupport() {
        validation.registerValidator(nameEdit) { control: Control,
                                                 value: String -> ValidationResult.fromErrorIf(control, null, value.isEmpty()) }
        validation.registerValidator(rateEdit, MainWindowController.BIG_DECIMAL_VALIDATOR)
        validation.initInitialDecoration()
    }
}
