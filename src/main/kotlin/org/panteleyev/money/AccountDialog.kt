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
import javafx.collections.FXCollections
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.ReadOnlyNamedConverter
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import org.panteleyev.utilities.fx.BaseDialog
import java.math.BigDecimal
import java.util.ResourceBundle
import java.util.UUID

class AccountDialog(val account: Account?, initialCategory: Category? = null)
    : BaseDialog<Account>(MainWindowController.CSS_PATH) {

    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val nameEdit = TextField()
    private val initialEdit = TextField()
    private val commentEdit = TextField()
    private val typeComboBox = ComboBox<CategoryType>()
    private val categoryComboBox = ComboBox<Category>()
    private val currencyComboBox = ComboBox<Currency>()
    private val activeCheckBox = CheckBox(rb.getString("account.Dialog.Active"))

    private var categories: Collection<Category>

    constructor(initialCategory: Category?) : this(null, initialCategory)

    init {
        title = rb.getString("account.Dialog.Title")

        dialogPane.content = GridPane().apply {
            styleClass.add(Styles.GRID_PANE)

            var index = 0
            addRow(index++, Label(rb.getString("label.Name")), nameEdit)
            addRow(index++, Label(rb.getString("label.Type")), typeComboBox)
            addRow(index++, Label(rb.getString("label.Category")), categoryComboBox)
            addRow(index++, Label(rb.getString("account.Dialog.InitialBalance")), initialEdit)
            addRow(index++, Label(rb.getString("label.Comment")), commentEdit)
            addRow(index++, Label(rb.getString("account.Dialog.Currency")), currencyComboBox)
            add(activeCheckBox, 1, index)
        }

        nameEdit.prefColumnCount = 20

        categories = MoneyDAO.getCategories()

        typeComboBox.converter = object : ReadOnlyStringConverter<CategoryType>() {
            override fun toString(obj: CategoryType): String = obj.typeName
        }
        categoryComboBox.converter = ReadOnlyNamedConverter<Category>()

        currencyComboBox.converter = object : ReadOnlyStringConverter<Currency>() {
            override fun toString(obj: Currency): String = obj.symbol
        }

        val currencyList = MoneyDAO.getCurrencies()
        currencyComboBox.items = FXCollections.observableArrayList(currencyList)
        typeComboBox.items = FXCollections.observableArrayList(*CategoryType.values())
        categoryComboBox.items = FXCollections.observableArrayList(categories)

        typeComboBox.setOnAction { onCategoryTypeSelected() }

        if (account == null) {
            nameEdit.text = ""
            initialEdit.text = "0.0"
            activeCheckBox.isSelected = true

            if (initialCategory != null) {
                typeComboBox.selectionModel
                        .select(initialCategory.type)
                onCategoryTypeSelected()
                categoryComboBox.selectionModel
                        .select(MoneyDAO.getCategory(initialCategory.id))
            } else {
                typeComboBox.selectionModel.select(0)
                onCategoryTypeSelected()
            }

            MoneyDAO.defaultCurrency?.let { currencyComboBox.selectionModel.select(it) }
        } else {
            nameEdit.text = account.name
            commentEdit.text = account.comment
            initialEdit.text = account.openingBalance.toString()
            activeCheckBox.isSelected = account.enabled

            typeComboBox.selectionModel
                    .select(account.type)
            categoryComboBox.selectionModel
                    .select(MoneyDAO.getCategory(account.categoryId))

            currencyComboBox.selectionModel
                    .select(MoneyDAO.getCurrency(account.currencyId))
        }

        setResultConverter { b: ButtonType ->
            if (b == ButtonType.OK) {
                // TODO: reconsider using null currency value
                val selectedCurrency = currencyComboBox.selectionModel.selectedItem

                return@setResultConverter Account(id = account?.id ?: 0,
                        name = nameEdit.text,
                        comment = commentEdit.text,
                        enabled = activeCheckBox.isSelected,
                        openingBalance = BigDecimal(initialEdit.text),
                        typeId = typeComboBox.selectionModel.selectedItem.id,
                        categoryId = categoryComboBox.selectionModel.selectedItem.id,
                        currencyId = selectedCurrency?.id ?: 0,
                        accountLimit = BigDecimal.ZERO,
                        currencyRate = BigDecimal.ONE,
                        guid = account?.guid ?: UUID.randomUUID().toString(),
                        modified = System.currentTimeMillis()
                )
            } else {
                return@setResultConverter null
            }
        }

        createDefaultButtons(rb)

        Platform.runLater { this.createValidationSupport() }
    }

    private fun onCategoryTypeSelected() {
        val type = typeComboBox.selectionModel.selectedItem

        val filtered = categories.filter { it.type == type }

        categoryComboBox.items = FXCollections.observableArrayList(filtered)

        if (!filtered.isEmpty()) {
            categoryComboBox.selectionModel.select(0)
        }
    }

    private fun createValidationSupport() {
        validation.registerValidator(nameEdit) {
            control: Control, value: String ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty())
        }
        validation.registerValidator(categoryComboBox) {
            control: Control, value: Category? ->
            ValidationResult.fromErrorIf(control, null, value == null)
        }
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR)
        validation.initInitialDecoration()
    }
}