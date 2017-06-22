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
import javafx.beans.property.SimpleStringProperty
import javafx.collections.MapChangeListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionFilter
import java.util.ResourceBundle
import java.util.function.Predicate

class RequestTab : BorderPane() {
    private val transactionTable = TransactionTableView(true)

    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val categoryTypeChoiceBox = ChoiceBox<Any>()
    private val categoryChoiceBox = ChoiceBox<Any>()
    private val accountChoiceBox = ChoiceBox<Any>()

    private val allTypesString = SimpleStringProperty()
    private val allCategoriesString = SimpleStringProperty()
    private val allAccountsString = SimpleStringProperty()

    private val categoryListener = MapChangeListener<Int, Category?> {
        Platform.runLater { setupCategoryBox(getSelectedCategoryType()) }
    }
    private val accountListener = MapChangeListener<Int, Account?> {
        Platform.runLater { setupAccountBox(getSelectedCategory()) }
    }

    init {
        val clearButton = Button(rb.getString("button.Clear"))
        clearButton.setOnAction { onClearButton() }

        val findButton = Button(rb.getString("button.Find"))
        findButton.setOnAction { onFindButton() }

        val row1 = HBox(5.0, clearButton, findButton)
        val row2 = HBox(5.0, Label(rb.getString("text.In.Semicolon")),
                categoryTypeChoiceBox, categoryChoiceBox, accountChoiceBox)
        row2.alignment = Pos.CENTER_LEFT

        val vBox = VBox(5.0, row1, row2)

        top = vBox
        center = transactionTable

        BorderPane.setMargin(vBox, Insets(5.0, 5.0, 5.0, 5.0))

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        allTypesString.set(rb.getString("account.Window.AllTypes"))
        allCategoriesString.set(rb.getString("account.Window.AllCategories"))
        allAccountsString.set(rb.getString("text.All.Accounts"))

        categoryTypeChoiceBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj: Any): String =  (obj as? CategoryType)?.typeName ?: obj.toString()
        }

        categoryChoiceBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj: Any): String = (obj as? Category)?.name ?: obj.toString()
        }

        accountChoiceBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj: Any): String = (obj as? Account)?.name ?: obj.toString()
        }

        categoryTypeChoiceBox.setOnAction {
            if (categoryTypeChoiceBox.selectionModel.selectedIndex == 0) {
                setupCategoryBox(null)
            } else {
                setupCategoryBox(categoryTypeChoiceBox.selectionModel.selectedItem as CategoryType?)
            }
        }

        categoryChoiceBox.setOnAction {
            if (categoryChoiceBox.selectionModel.selectedIndex == 0) {
                setupAccountBox(null)
            } else {
                setupAccountBox(categoryChoiceBox.selectionModel.selectedItem as Category?)
            }
        }

        transactionTable.setOnCheckTransaction({
            transactions, check -> onCheckTransaction(transactions, check)
        })

        with (MoneyDAO) {
            categories().addListener(categoryListener)
            accounts().addListener(accountListener)

            preloadingProperty().addListener { _, _, newValue ->
                if (!newValue) {
                    Platform.runLater { setupCategoryTypesBox() }
                }
            }
        }
    }

    private fun setupCategoryTypesBox() {
        with (categoryTypeChoiceBox) {
            with (items) {
                clear()
                add(allTypesString.get())
                add(Separator())
                addAll(*CategoryType.values())
            }

            selectionModel.clearAndSelect(0)
        }

        setupCategoryBox(null)
    }

    private fun setupCategoryBox(type: CategoryType?) {
        categoryChoiceBox.items.clear()
        categoryChoiceBox.items.add(allCategoriesString.get())

        if (type != null) {
            MoneyDAO.getCategoriesByType(type)
                    .forEach { categoryChoiceBox.items.add(it) }
        }

        categoryChoiceBox.selectionModel.clearAndSelect(0)

        setupAccountBox(null)
    }

    private fun setupAccountBox(category: Category?) {
        accountChoiceBox.items.clear()
        accountChoiceBox.items.add(allAccountsString.get())

        if (category != null) {
            MoneyDAO.getAccountsByCategory(category.id)
                    .filter { it.enabled }
                    .forEach { accountChoiceBox.items.add(it) }
        }

        accountChoiceBox.selectionModel.clearAndSelect(0)
    }

    private fun getSelectedAccount(): Account? {
        val obj = accountChoiceBox.selectionModel.selectedItem
        if (obj is Account) {
            return obj
        } else {
            return null
        }
    }

    private fun getSelectedCategory(): Category? {
        val obj = categoryChoiceBox.selectionModel.selectedItem
        if (obj is Category) {
            return obj
        } else {
            return null
        }
    }

    private fun getSelectedCategoryType(): CategoryType? {
        val obj = categoryTypeChoiceBox.selectionModel.selectedItem
        if (obj is CategoryType) {
            return obj
        } else {
            return null
        }
    }

    private fun onFindButton() {
        var filter : Predicate<Transaction> = Predicate { true }

        val account = getSelectedAccount()
        if (account != null) {
            filter = TransactionFilter.byAccount(account.id)
        } else {
            val category = getSelectedCategory()
            if (category != null) {
                filter = TransactionFilter.byCategory(category.id)
            } else {
                val type = getSelectedCategoryType()
                if (type != null) {
                    filter = TransactionFilter.byCategoryType(type.id)
                }
            }
        }

        transactionTable.setTransactionFilter(filter)
    }

    private fun onClearButton() {
        transactionTable.setTransactionFilter(Predicate { false })
        setupCategoryTypesBox()
    }

    private fun onCheckTransaction(transactions: List<Transaction>, check: Boolean?) {
        transactions.forEach { t ->
            MoneyDAO.updateTransaction(Transaction.Builder(t)
                    .checked(check!!)
                    .build())
        }
    }
}