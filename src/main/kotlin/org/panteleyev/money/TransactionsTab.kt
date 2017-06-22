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
import javafx.collections.MapChangeListener
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TableColumn
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionGroup
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.ResourceBundle
import java.util.function.Predicate

internal class TransactionsTab : BorderPane() {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val accountFilterBox = ChoiceBox<Any>()
    private val monthFilterBox = ChoiceBox<String>()
    private val yearSpinner = Spinner<Int>()
    private val transactionCountLabel = Label()

    private val transactionTable = TransactionTableView(false)
    val transactionEditor = TransactionEditorPane()

    private val accountListener = MapChangeListener<Int, Account> {
        Platform.runLater { this.initAccountFilterBox() }
        Platform.runLater { this.reloadTransactions() }
    }

    init {
        val prevButton = Button("", getButtonImage("arrow-left-16.png"))
        prevButton.setOnAction { onPrevMonth() }

        val todayButton = Button("", getButtonImage("bullet-black-16.png"))
        todayButton.setOnAction { onCurrentMonth() }

        val nextButton = Button("", getButtonImage("arrow-right-16.png"))
        nextButton.setOnAction { onNextMonth() }

        monthFilterBox.setOnAction { onMonthChanged() }

        val hBox = HBox(5.0,
                accountFilterBox,
                monthFilterBox,
                yearSpinner,
                prevButton,
                todayButton,
                nextButton,
                Label("Transactions:"),
                transactionCountLabel
        )
        hBox.alignment = Pos.CENTER_LEFT

        top = hBox
        BorderPane.setMargin(hBox, Insets(5.0, 5.0, 5.0, 5.0))

        transactionTable.setOnMouseClicked { onTransactionSelected() }

        transactionCountLabel.textProperty().bind(transactionTable.listSizeProperty().asString())

        center = transactionTable
        bottom = transactionEditor

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Workaround for https://bugs.openjdk.java.net/browse/JDK-8146356
        var textStyle = TextStyle.FULL_STANDALONE
        val testMonth = Month.JANUARY.getDisplayName(textStyle, Locale.getDefault())
        if (testMonth == "1") {
            textStyle = TextStyle.FULL
        } else {
            Logging.logger.info("JDK-8146356 has been resolved")
        }

        for (i in 1..12) {
            monthFilterBox.items
                    .add(Month.of(i).getDisplayName(textStyle, Locale.getDefault()))
        }

        val valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1970, 2050)
        yearSpinner.valueFactory = valueFactory
        yearSpinner.valueProperty().addListener { _, _, _ -> Platform.runLater { this.reloadTransactions() } }

        transactionEditor.setOnAddTransaction({ builder, c -> onAddTransaction(builder, c) })
        transactionEditor.setOnUpdateTransaction({ builder, c -> onUpdateTransaction(builder, c) })
        transactionEditor.setOnDeleteTransaction({ onDeleteTransaction(it) })

        transactionTable.setOnAddGroup({ builder, transactions -> this.onAddGroup(builder, transactions) })
        transactionTable.setOnDeleteGroup({ onDeleteGroup(it) })
        transactionTable.setOnCheckTransaction({ transactions, check -> this.onCheckTransaction(transactions, check) })

        setCurrentDate()

        accountFilterBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj: Any): String? {
                if (obj is String) {
                    return obj.toString()
                }

                if (obj is Account) {
                    when (obj.type) {
                        CategoryType.BANKS_AND_CASH -> return "[" + obj.name + "]"
                        CategoryType.INCOMES -> return "+ " + obj.name
                        CategoryType.EXPENSES -> return "- " + obj.name
                        CategoryType.DEBTS -> return "! " + obj.name
                        CategoryType.ASSETS -> return ". " + obj.name
                        else -> return obj.name
                    }
                }

                return null
            }
        }

        accountFilterBox.selectionModel.selectedIndexProperty()
                .addListener { _, _, _ -> Platform.runLater { this.reloadTransactions() } }

        with (MoneyDAO) {
            accounts().addListener(accountListener)

            preloadingProperty().addListener { _, _, newValue ->
                if (!newValue) {
                    Platform.runLater { initAccountFilterBox() }
                    Platform.runLater { reloadTransactions() }
                }
            }
        }
    }

    private fun getButtonImage(name: String): ImageView {
        val image = ImageView(Image("/org/panteleyev/money/res/" + name))
        image.fitHeight = 16.0
        image.fitWidth = 16.0
        return image
    }


    private fun addAccountsToChoiceBox(aList: Collection<Account>) {
        if (!aList.isEmpty()) {
            accountFilterBox.items.add(Separator())

            aList.filter { it.enabled }
                    .sortedWith(Comparator<Account> { a1, a2 -> a1.name.compareTo(a2.name, ignoreCase = true)})
                    .forEach { accountFilterBox.items.add(it) }
        }
    }

    private fun initAccountFilterBox() {
        accountFilterBox.items.setAll(
                rb.getString("text.All.Accounts")
        )

        addAccountsToChoiceBox(MoneyDAO.getAccountsByType(CategoryType.BANKS_AND_CASH))
        addAccountsToChoiceBox(MoneyDAO.getAccountsByType(CategoryType.DEBTS))
        addAccountsToChoiceBox(MoneyDAO.getAccountsByType(CategoryType.ASSETS))

        accountFilterBox.selectionModel.clearAndSelect(0)
    }

    private fun setCurrentDate() {
        val cal = Calendar.getInstance()
        monthFilterBox.selectionModel.select(cal.get(Calendar.MONTH))
        yearSpinner.valueFactory.value = cal.get(Calendar.YEAR)
    }

    private fun onPrevMonth() {
        var month = monthFilterBox.selectionModel.selectedIndex - 1

        if (month < 0) {
            month = 11
            yearSpinner.valueFactory.decrement(1)
        }

        monthFilterBox.selectionModel.select(month)

        reloadTransactions()
    }

    private fun onNextMonth() {
        var month = monthFilterBox.selectionModel.selectedIndex + 1

        if (month == 12) {
            month = 0
            yearSpinner.valueFactory.increment(1)
        }

        monthFilterBox.selectionModel.select(month)

        reloadTransactions()
    }

    private fun onCurrentMonth() {
        setCurrentDate()

        reloadTransactions()
    }

    private fun reloadTransactions() {
        transactionTable.clear()
        transactionEditor.clear()

        transactionTable.selectionModel.select(null)

        val month = monthFilterBox.selectionModel.selectedIndex + 1
        val year = yearSpinner.value

        var filter = Predicate<Transaction> { it.month == month && it.year == year }

        val selected = accountFilterBox.selectionModel.selectedItem
        if (selected is Account) {
            val id = selected.id
            filter = filter.and { it.accountCreditedId == id || it.accountDebitedId == id }
        }

        transactionTable.setTransactionFilter(filter)
        transactionTable.sort()
    }

    private fun onMonthChanged() {
        reloadTransactions()
    }

    private fun onTransactionSelected() {
        transactionEditor.clear()
        if (transactionTable.selectedTransactionCount == 1) {
            transactionTable.selectedTransaction?.let{ transactionEditor.setTransaction(it) }
        }
    }

    private fun createContact(name: String): Contact {
        return MoneyDAO.insertContact(Contact(
                _id = MoneyDAO.generatePrimaryKey(Contact::class.java),
                name = name))
    }

    private fun onAddTransaction(builder: Transaction.Builder, c: String?) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).id)
        }

        // date
        val month = monthFilterBox.selectionModel.selectedIndex + 1
        val year = yearSpinner.value

        builder.id(MoneyDAO.generatePrimaryKey(Transaction::class.java)!!)
                .month(month)
                .year(year)
                .groupId(0)

        transactionEditor.clear()
        MoneyDAO.insertTransaction(builder.build())
    }

    private fun onUpdateTransaction(builder: Transaction.Builder, c: String?) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).id)
        }

        // date
        val month = monthFilterBox.selectionModel.selectedIndex + 1
        builder.month(month)
        val year = yearSpinner.value
        builder.year(year)

        transactionEditor.clear()
        MoneyDAO.updateTransaction(builder.build())
    }

    private fun onAddGroup(group: TransactionGroup, transactions: List<Transaction>) {
        val groupId = MoneyDAO.generatePrimaryKey(TransactionGroup::class.java)!!

        val grp = group.copy(_id = groupId)

        transactions.forEach {
            MoneyDAO.updateTransaction(
                    Transaction.Builder(it)
                            .groupId(groupId)
                            .build()
            )
        }

        MoneyDAO.insertTransactionGroup(grp)
    }

    private fun onDeleteGroup(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            Logging.logger.warning("Attempt to delete empty transaction group")
            return
        }

        val groupId = transactions[0].groupId

        transactions.forEach { t ->
            MoneyDAO.updateTransaction(
                    Transaction.Builder(t)
                            .groupId(0)
                            .build()
            )
        }

        MoneyDAO.deleteTransactionGroup(groupId)
    }

    private fun onDeleteTransaction(id: Int?) {
        Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
                .showAndWait()
                .ifPresent { r ->
                    if (r == ButtonType.OK) {
                        transactionEditor.clear()
                        MoneyDAO.deleteTransaction(id!!)
                    }
                }
    }

    private fun onCheckTransaction(transactions: List<Transaction>, check: Boolean?) {
        transactions.forEach { t ->
            MoneyDAO.updateTransaction(
                    Transaction.Builder(t)
                            .checked(check!!)
                            .build()
            )
        }
    }

    fun scrollToEnd() {
        if (transactionTable.dayColumn.sortType == TableColumn.SortType.ASCENDING) {
            transactionTable.scrollTo(MoneyDAO.transactions().size - 1)
        } else {
            transactionTable.scrollTo(0)
        }
    }
}
