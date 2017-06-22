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
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.MapChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.MenuButton
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.Callback
import javafx.util.StringConverter
import org.controlsfx.control.textfield.AutoCompletionBinding
import org.controlsfx.control.textfield.TextFields
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.Named
import org.panteleyev.money.persistence.Transaction
import org.panteleyev.money.persistence.TransactionType
import org.panteleyev.persistence.annotations.Field
import java.math.BigDecimal
import java.util.Calendar
import java.util.ResourceBundle
import java.util.TreeSet

class TransactionEditorPane : TitledPane() {

    private abstract class BaseCompletionProvider<T> constructor(private val set: Set<T>) : Callback<AutoCompletionBinding.ISuggestionRequest, Collection<T>> {

        internal abstract fun getElementString(element: T): String

        override fun call(req: AutoCompletionBinding.ISuggestionRequest): Collection<T> {
            if (req.userText.length >= Options.getAutoCompleteLength()) {
                val userText = req.userText

                val result = set.filter {
                    getElementString(it).toLowerCase().contains(userText.toLowerCase())
                }

                if (result.size == 1 && getElementString(result[0]) == userText) {
                    /* If there is a single case sensitive match then no suggestions must be shown. */
                    return emptyList()
                } else {
                    return result
                }
            } else {
                return emptyList()
            }
        }
    }

    private class CompletionProvider<T : Named> constructor(set: Set<T>) : BaseCompletionProvider<T>(set) {
        override fun getElementString(element: T): String = element.name
    }

    private class TransactionTypeCompletionProvider constructor(set: Set<TransactionType>)
        : BaseCompletionProvider<TransactionType>(set)
    {
        override fun getElementString(element: TransactionType): String = element.typeName
    }

    private class StringCompletionProvider constructor(set: Set<String>) : BaseCompletionProvider<String>(set) {
        override fun getElementString(element: String): String = element
    }

    private val rb = ResourceBundle.getBundle("org.panteleyev.money.res.TransactionEditorPane")

    private val daySpinner = Spinner<Int>()

    private val typeEdit = TextField()
    private val debitedAccountEdit = TextField()
    private val creditedAccountEdit = TextField()
    private val contactEdit = TextField()
    private val sumEdit = TextField()
    private val checkedCheckBox = CheckBox()
    private val commentEdit = TextField()
    private val rate1Edit = TextField()
    private val rateDir1Combo = ComboBox<String>()
    private val invoiceNumberEdit = TextField()
    private val rateAmoutLabel = Label()
    private val debitedCategoryLabel = Label()
    private val creditedCategoryLabel = Label()

    private val typeMenuButton = MenuButton()
    private val debitedMenuButton = MenuButton()
    private val creditedMenuButton = MenuButton()
    private val contactMenuButton = MenuButton()

    private val addButton = Button(rb.getString("addButton"))
    private val updateButton = Button(rb.getString("updateButton"))
    private val deleteButton = Button(rb.getString("deleteButton"))
    private val clearButton = Button(rb.getString("clearButton"))

    private var addTransactionConsumer: (Transaction.Builder, String) -> Unit = { _,_ -> }
    private var updateTransactionConsumer: (Transaction.Builder, String) -> Unit = { _,_ -> }
    private var deleteTransactionConsumer: (Int) -> Unit = { }

    private var builder = Transaction.Builder()

    private val typeSuggestions = TreeSet<TransactionType>()
    private val contactSuggestions = TreeSet<Contact>()
    private val debitedSuggestions = TreeSet<Account>()
    private val creditedSuggestions = TreeSet<Account>()
    private val commentSuggestions = TreeSet<String>()

    private val validation = ValidationSupport()

    private val newTransactionProperty = SimpleBooleanProperty(true)

    private val DECIMAL_VALIDATOR = { control: Control, value: String ->
        var invalid = false
        try {
            BigDecimal(value)
            updateRateAmount()
        } catch (ex: NumberFormatException) {
            invalid = true
        }

        ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled)
    }

    private var newContactName: String = ""

    private val contactListener = MapChangeListener<Int, Contact> { Platform.runLater { setupContactMenu() } }
    private val accountListener = MapChangeListener<Int, Account> { Platform.runLater { setupAccountMenus() } }
    private val transactionListener = MapChangeListener<Int, Transaction> { transactionChangeListener(it) }

    init {
        val debitedBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("debitedAccountLabel")),
                HBox(debitedAccountEdit, debitedMenuButton),
                debitedCategoryLabel)
        HBox.setHgrow(debitedAccountEdit, Priority.ALWAYS)

        val creditedBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("creditedAccountLabel")),
                HBox(creditedAccountEdit, creditedMenuButton),
                creditedCategoryLabel)
        HBox.setHgrow(creditedAccountEdit, Priority.ALWAYS)

        val contactBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("contactLabel")),
                HBox(contactEdit, contactMenuButton))
        HBox.setHgrow(contactEdit, Priority.ALWAYS)

        val hBox1 = HBox(Styles.BIG_SPACING, sumEdit, checkedCheckBox)
        hBox1.alignment = Pos.CENTER_LEFT
        val sumBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("sumLabel")), hBox1)

        val commentBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("commentLabel")), commentEdit)

        val rateBox = VBox(Styles.SMALL_SPACING, Label(rb.getString("rateLabel")),
                HBox(rate1Edit, rateDir1Combo),
                rateAmoutLabel)

        val filler = Region()

        clearButton.setOnAction { onClearButton() }
        clearButton.isCancelButton = true

        addButton.setOnAction { onAddButton() }
        addButton.isDefaultButton = true

        updateButton.setOnAction { onUpdateButton() }

        deleteButton.setOnAction { onDeleteButton() }

        val row3 = HBox(Styles.BIG_SPACING,
                VBox(2.0, Label(rb.getString("invoiceLabel")), invoiceNumberEdit),
                filler, clearButton, deleteButton, updateButton, addButton)
        row3.alignment = Pos.CENTER_LEFT

        content = VBox(Styles.BIG_SPACING,
                HBox(Styles.BIG_SPACING,
                        VBox(2.0, Label(rb.getString("dayLabel")), daySpinner),
                        VBox(2.0, Label(rb.getString("typeLabel")),
                                HBox(typeEdit, typeMenuButton)),
                        debitedBox, creditedBox, contactBox, sumBox),
                HBox(Styles.BIG_SPACING, commentBox, rateBox),
                row3)

        HBox.setHgrow(debitedBox, Priority.ALWAYS)
        HBox.setHgrow(creditedBox, Priority.ALWAYS)
        HBox.setHgrow(contactBox, Priority.ALWAYS)
        HBox.setHgrow(commentBox, Priority.ALWAYS)
        HBox.setHgrow(filler, Priority.ALWAYS)

        typeMenuButton.isFocusTraversable = false
        checkedCheckBox.isFocusTraversable = false
        debitedMenuButton.isFocusTraversable = false
        creditedMenuButton.isFocusTraversable = false
        contactMenuButton.isFocusTraversable = false

        rate1Edit.isDisable = true
        rate1Edit.prefColumnCount = 5

        rateDir1Combo.isDisable = true

        rateAmoutLabel.styleClass.add(Styles.RATE_LABEL)
        debitedCategoryLabel.styleClass.add(Styles.SUB_LABEL)
        creditedCategoryLabel.styleClass.add(Styles.SUB_LABEL)

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        text = rb.getString("title") + " ###"

        daySpinner.isEditable = true
        daySpinner.editor.prefColumnCountProperty().set(4)
        val valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1, 1)
        valueFactory.value = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        daySpinner.valueFactory = valueFactory

        deleteButton.disableProperty().bind(newTransactionProperty)
        updateButton.disableProperty().bind(validation.invalidProperty().or(newTransactionProperty))
        addButton.disableProperty().bind(validation.invalidProperty())

        rateDir1Combo.items.setAll("/", "*")

        TextFields.bindAutoCompletion(typeEdit, TransactionTypeCompletionProvider(typeSuggestions), TRANSACTION_TYPE_TO_STRING)
        TextFields.bindAutoCompletion(debitedAccountEdit, CompletionProvider(debitedSuggestions), ACCOUNT_TO_STRING)
        TextFields.bindAutoCompletion(creditedAccountEdit, CompletionProvider(creditedSuggestions), ACCOUNT_TO_STRING)
        TextFields.bindAutoCompletion(contactEdit, CompletionProvider(contactSuggestions), CONTACT_TO_STRING)
        TextFields.bindAutoCompletion(commentEdit, StringCompletionProvider(commentSuggestions))

        Platform.runLater { createValidationSupport() }

        creditedAccountEdit.focusedProperty().addListener { _, oldValue, newValue ->
            if (oldValue!! && !newValue) {
                processAutoFill()
            }
        }

        typeEdit.focusedProperty().addListener { _, oldValue, newValue ->
            if (oldValue!! && !newValue) {
                autoFillType()
            }
        }

        with (MoneyDAO) {
            contacts().addListener(contactListener)
            accounts().addListener(accountListener)
            transactions().addListener(transactionListener)

            preloadingProperty().addListener { _, _, newValue ->
                if (!newValue) {
                    Platform.runLater {
                        onChangedTransactionTypes()
                        setupAccountMenus()
                        setupContactMenu()
                        setupComments()
                    }
                }
            }
        }
    }

    fun initControls() {
        contactEdit.text = ""

        typeMenuButton.items.clear()
        debitedMenuButton.items.clear()
        creditedMenuButton.items.clear()
        contactMenuButton.items.clear()

        typeSuggestions.clear()
        contactSuggestions.clear()
        debitedSuggestions.clear()
        creditedSuggestions.clear()
        commentSuggestions.clear()
    }

    private fun setupBanksAndCashMenuItems(debitedSuggestions: MutableSet<Account>, creditedSuggestions: MutableSet<Account>) {
        val banksAndCash = MoneyDAO.getAccountsByType(CategoryType.BANKS_AND_CASH)
                .filter { it.enabled }

        banksAndCash.sortedWith(Comparator<Account> { a1, a2 -> a1.name.compareTo(a2.name, ignoreCase = true) })
                .forEach { acc ->
                    val title = "[" + acc.name + "]"
                    val m1 = MenuItem(title)
                    m1.setOnAction { onDebitedAccountSelected(acc) }
                    val m2 = MenuItem(title)
                    m2.setOnAction { onCreditedAccountSelected(acc) }

                    debitedMenuButton.items.add(m1)
                    creditedMenuButton.items.add(m2)

                    debitedSuggestions.add(acc)
                    creditedSuggestions.add(acc)
                }

        if (!banksAndCash.isEmpty()) {
            debitedMenuButton.items.add(SeparatorMenuItem())
            creditedMenuButton.items.add(SeparatorMenuItem())
        }
    }

    private fun setupDebtMenuItems(debitedSuggestions: MutableSet<Account>, creditedSuggestions: MutableSet<Account>) {
        setAccountMenuItemsByCategory(CategoryType.DEBTS, "!", debitedSuggestions, creditedSuggestions)
    }

    private fun setupAssetsMenuItems(debitedSuggestions: MutableSet<Account>, creditedSuggestions: MutableSet<Account>) {
        setAccountMenuItemsByCategory(CategoryType.ASSETS, ".", debitedSuggestions, creditedSuggestions)
    }

    private fun setAccountMenuItemsByCategory(categoryType: CategoryType, prefix: String, debitedSuggestions: MutableSet<Account>, creditedSuggestions: MutableSet<Account>) {
        val categories = MoneyDAO.getCategoriesByType(categoryType)
        categories.sortedWith(Comparator<Category> { c1, c2 -> c1.name.compareTo(c2.name, ignoreCase = true)})
                .forEach { x ->
                    val accounts = MoneyDAO.getAccountsByCategory(x.id)

                    if (!accounts.isEmpty()) {
                        debitedMenuButton.items.add(MenuItem(x.name))
                        creditedMenuButton.items.add(MenuItem(x.name))

                        accounts.filter{ it.enabled }
                                .forEach { acc ->
                                    val title = "  " + prefix + " " + acc.name
                                    val m1 = MenuItem(title)
                                    m1.setOnAction { onDebitedAccountSelected(acc) }
                                    val m2 = MenuItem(title)
                                    m2.setOnAction { onCreditedAccountSelected(acc) }

                                    debitedMenuButton.items.add(m1)
                                    creditedMenuButton.items.add(m2)

                                    debitedSuggestions.add(acc)
                                    creditedSuggestions.add(acc)
                                }
                    }
                }

        if (!categories.isEmpty()) {
            debitedMenuButton.items.add(SeparatorMenuItem())
            creditedMenuButton.items.add(SeparatorMenuItem())
        }
    }

    fun clear() {
        builder = Transaction.Builder()

        newTransactionProperty.set(true)

        daySpinner.valueFactory.value = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        typeEdit.text = ""
        creditedAccountEdit.text = ""
        debitedAccountEdit.text = ""
        contactEdit.text = ""
        checkedCheckBox.isSelected = false
        commentEdit.text = ""
        checkedCheckBox.isSelected = false
        invoiceNumberEdit.text = ""
        sumEdit.text = ""
        rateAmoutLabel.text = ""


        daySpinner.editor.text = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        daySpinner.editor.selectAll()
        //        daySpinner.requestFocus();
    }

    fun setTransaction(tr: Transaction) {
        builder = Transaction.Builder(tr)

        newTransactionProperty.set(false)

        text = rb.getString("title") + " #" + tr.id

        // Type
        typeEdit.text = tr.transactionType.typeName

        // Accounts
        val accCredited : Account? = MoneyDAO.getAccount(tr.accountCreditedId)
        creditedAccountEdit.text = accCredited?.name?:""

        val accDebited : Account? = MoneyDAO.getAccount(tr.accountDebitedId)
        debitedAccountEdit.text = accDebited?.name?:""

        contactEdit.text = MoneyDAO.getContact(tr.contactId)?.name?:""

        // Other fields
        commentEdit.text = tr.comment
        checkedCheckBox.isSelected = tr.checked
        invoiceNumberEdit.text = tr.invoiceNumber

        // Rate

        val debitedCurrencyId = accDebited?.currencyId?:0
        val creditedCurrencyId = accCredited?.currencyId?:0

        if (debitedCurrencyId == creditedCurrencyId) {
            rate1Edit.isDisable = true
            rate1Edit.text = ""
        } else {
            rate1Edit.isDisable = false

            var rate: BigDecimal? = tr.rate
            if (BigDecimal.ZERO.compareTo(rate!!) == 0) {
                rate = BigDecimal.ONE.setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP)
            }
            if (rate != null) {
                rate1Edit.text = rate.toString()
                rateDir1Combo.selectionModel.select(tr.rateDirection)
            } else {
                rate1Edit.text = ""
            }
        }

        // Day
        daySpinner.valueFactory.value = tr.day

        // Sum
        sumEdit.text = tr.amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
        updateRateAmount()
    }

    private fun onClearButton() {
        clear()
        daySpinner.requestFocus()
    }

    private fun onDeleteButton() {
        if (builder.id != 0) {
            deleteTransactionConsumer(builder.id)
        }
    }

    private fun onUpdateButton() {
        if (buildTransaction()) {
            updateTransactionConsumer(builder, newContactName)
            daySpinner.requestFocus()
        }
    }

    private fun onAddButton() {
        if (buildTransaction()) {
            addTransactionConsumer(builder, newContactName)
            daySpinner.requestFocus()
        }
    }

    private fun onContactSelected(c: Contact) {
        contactEdit.text = c.name
        builder.contactId(c.id)
    }

    private fun onDebitedAccountSelected(acc: Account) {
        debitedAccountEdit.text = acc.name
        enableDisableRate()
    }

    private fun onCreditedAccountSelected(acc: Account) {
        creditedAccountEdit.text = acc.name
        enableDisableRate()
    }

    private fun onTransactionTypeSelected(type: TransactionType) {
        typeEdit.text = type.typeName
    }

    fun setOnAddTransaction(c: (Transaction.Builder, String) -> Unit) {
        addTransactionConsumer = c
    }

    fun setOnUpdateTransaction(c: (Transaction.Builder, String) -> Unit) {
        updateTransactionConsumer = c
    }

    fun setOnDeleteTransaction(c: (Int) -> Unit) {
        deleteTransactionConsumer = c
    }

    private fun buildTransaction(): Boolean {
        // Check type id
        handleTypeFocusLoss()
        val type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING)
        type?.let { builder.transactionType(it) }

        val debitedAccount = checkTextFieldValue(debitedAccountEdit, debitedSuggestions, ACCOUNT_TO_STRING)
        if (debitedAccount != null) {
            builder.accountDebitedId(debitedAccount.id)
            builder.accountDebitedCategoryId(debitedAccount.categoryId)
            builder.accountDebitedType(debitedAccount.type)
        } else {
            return false
        }

        val creditedAccount = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING)
        if (creditedAccount != null) {
            builder.accountCreditedId(creditedAccount.id)
            builder.accountCreditedCategoryId(creditedAccount.categoryId)
            builder.accountCreditedType(creditedAccount.type)
        } else {
            return false
        }

        // builder.day(daySpinner.getValue());
        builder.comment(commentEdit.text)
        builder.checked(checkedCheckBox.isSelected)
        builder.invoiceNumber(invoiceNumberEdit.text)

        try {
            builder.day(Integer.parseInt(daySpinner.editor.text))

            builder.amount(BigDecimal(sumEdit.text))

            if (!rate1Edit.isDisabled) {
                builder.rate(BigDecimal(rate1Edit.text))
                builder.rateDirection(rateDir1Combo.selectionModel.selectedIndex)
            } else {
                builder.rate(BigDecimal.ONE)
                builder.rateDirection(1)
            }
        } catch (ex: NumberFormatException) {
            return false
        }

        newContactName = ""

        val contactName = contactEdit.text
        if (contactName == null || contactName.isEmpty()) {
            builder.contactId(0)
        } else {
            val contact = checkTextFieldValue(contactName, contactSuggestions, CONTACT_TO_STRING)
            if (contact != null) {
                builder.contactId(contact.id)
            } else {
                newContactName = contactName
            }
        }

        return true
    }

    private fun enableDisableRate() {
        val disable: Boolean

        if (builder.accountCreditedId == 0 || builder.accountDebitedId == 0) {
            disable = true
        } else {
            val c1 = MoneyDAO.getAccount(builder.accountDebitedId)?.currencyId?:0
            val c2 = MoneyDAO.getAccount(builder.accountCreditedId)?.currencyId?:0

            disable = c1 == c2
        }

        rate1Edit.isDisable = disable
        rateDir1Combo.isDisable = disable

        if (!disable && rate1Edit.text.isEmpty()) {
            rate1Edit.text = "1"
            rateDir1Combo.selectionModel.select(0)
        }
    }

    private fun <T : Named> checkTextFieldValue(value: String, items: Collection<T>, converter: StringConverter<T>): T? {
        return items.find { converter.toString(it) == value }
    }

    private fun checkTransactionTypeFieldValue(value: String, items: Collection<TransactionType>, converter: StringConverter<TransactionType>): TransactionType? {
        return items.find { converter.toString(it) == value }
    }

    private fun <T : Named> checkTextFieldValue(field: TextField, items: Collection<T>, converter: StringConverter<T>): T? {
        return checkTextFieldValue(field.text, items, converter)
    }

    private fun checkTransactionTypeFieldValue(field: TextField, items: Collection<TransactionType>, converter: StringConverter<TransactionType>): TransactionType? {
        return checkTransactionTypeFieldValue(field.text, items, converter)
    }

    private fun handleTypeFocusLoss() {
        val type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING)
        if (type == null) {
            typeEdit.text = TransactionType.UNDEFINED.typeName
        }
    }

    private fun createValidationSupport() {
        validation.registerValidator(typeEdit) { control: Control, _: String ->
            val type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING)
            ValidationResult.fromErrorIf(control, null, type == null)
        }

        validation.registerValidator(debitedAccountEdit) { control: Control, _: String ->
            val account = checkTextFieldValue(debitedAccountEdit, debitedSuggestions, ACCOUNT_TO_STRING)
            updateCategoryLabel(debitedCategoryLabel, account)

            builder.accountDebitedId(account?.id?:0)

            enableDisableRate()
            ValidationResult.fromErrorIf(control, null, account == null)
        }

        validation.registerValidator(creditedAccountEdit) { control: Control, _: String ->
            val account = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING)
            updateCategoryLabel(creditedCategoryLabel, account)

            builder.accountCreditedId(account?.id?:0)

            enableDisableRate()
            ValidationResult.fromErrorIf(control, null, account == null)
        }

        validation.registerValidator(sumEdit, DECIMAL_VALIDATOR)
        validation.registerValidator(rate1Edit, false, DECIMAL_VALIDATOR)

        validation.initInitialDecoration()
    }

    private fun setupContactMenu() {
        contactMenuButton.items.clear()
        contactSuggestions.clear()

        val contacts = MoneyDAO.getContacts()
        contacts.sortedWith(Comparator<Contact> {c1, c2 -> c1.name.compareTo(c2.name, ignoreCase = true)})
                .forEach { x ->
                    val m = MenuItem(x.name)
                    m.setOnAction { onContactSelected(x) }
                    contactMenuButton.items.add(m)
                    contactSuggestions.add(x)
                }

        contactMenuButton.isDisable = contactMenuButton.items.isEmpty()
    }

    private fun onChangedTransactionTypes() {
        typeMenuButton.items.clear()
        typeSuggestions.clear()

        TransactionType.values().forEach { x ->
            if (x.separator) {
                typeMenuButton.items.add(SeparatorMenuItem())
            } else {
                val m = MenuItem(x.typeName)
                m.setOnAction { onTransactionTypeSelected(x) }
                typeMenuButton.items.add(m)
                typeSuggestions.add(x)
            }
        }
    }

    private fun setupAccountMenus() {
        debitedMenuButton.items.clear()
        creditedMenuButton.items.clear()
        debitedSuggestions.clear()
        creditedSuggestions.clear()

        // Bank and cash accounts first
        setupBanksAndCashMenuItems(debitedSuggestions, creditedSuggestions)

        // Incomes to debitable accounts
        val incomeCategories = MoneyDAO.getCategoriesByType(CategoryType.INCOMES)
        incomeCategories.sortedWith(Comparator<Category> {c1, c2 -> c1.name.compareTo(c2.name, ignoreCase = true)})
                .forEach { x ->
                    val accounts = MoneyDAO.getAccountsByCategory(x.id)

                    if (!accounts.isEmpty()) {
                        debitedMenuButton.items.add(MenuItem(x.name))

                        accounts.forEach { acc ->
                            val accMenuItem = MenuItem("  + " + acc.name)
                            accMenuItem.setOnAction { onDebitedAccountSelected(acc) }
                            debitedMenuButton.items.add(accMenuItem)
                            debitedSuggestions.add(acc)
                        }
                    }
                }

        if (!incomeCategories.isEmpty()) {
            debitedMenuButton.items.add(SeparatorMenuItem())
        }

        // Expenses to creditable accounts
        val expenseCategories = MoneyDAO.getCategoriesByType(CategoryType.EXPENSES)
        expenseCategories.sortedWith(Comparator<Category> {c1, c2 -> c1.name.compareTo(c2.name, ignoreCase = true)})
                .forEach { x ->
                    val accounts = MoneyDAO.getAccountsByCategory(x.id)

                    if (!accounts.isEmpty()) {
                        creditedMenuButton.items.add(MenuItem(x.name))

                        accounts.forEach { acc ->
                            creditedSuggestions.add(acc)
                            val accMenuItem = MenuItem("  - " + acc.name)
                            accMenuItem.setOnAction { onCreditedAccountSelected(acc) }
                            creditedMenuButton.items.add(accMenuItem)
                        }
                    }
                }

        if (!expenseCategories.isEmpty()) {
            creditedMenuButton.items.add(SeparatorMenuItem())
        }

        setupDebtMenuItems(debitedSuggestions, creditedSuggestions)
        setupAssetsMenuItems(debitedSuggestions, creditedSuggestions)
    }

    private fun setupComments() {
        commentSuggestions.clear()
        commentSuggestions.addAll(MoneyDAO.uniqueTransactionComments)
    }

    private fun transactionChangeListener(change: MapChangeListener.Change<out Int, out Transaction>) {
        if (change.wasAdded()) {
            val comment = change.valueAdded.comment
            if (!comment.isEmpty()) {
                commentSuggestions.add(comment)
            }
        }
    }

    private fun updateRateAmount() {
        var amount = sumEdit.text
        if (amount.isEmpty()) {
            amount = "0"
        }

        val amountValue = BigDecimal(amount)
                .setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP)

        var rate = rate1Edit.text
        if (rate.isEmpty()) {
            rate = "1"
        }

        val rateValue = BigDecimal(rate)
                .setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP)

        val total: BigDecimal

        if (rateDir1Combo.selectionModel.selectedIndex == 0) {
            total = amountValue.divide(rateValue, BigDecimal.ROUND_HALF_UP)
        } else {
            total = amountValue.multiply(rateValue)
        }

        Platform.runLater { rateAmoutLabel.text = "= " + total.setScale(2, BigDecimal.ROUND_HALF_UP).toString() }
    }

    private fun updateCategoryLabel(label: Label, account: Account?) {
        if (account != null) {
            val catName = MoneyDAO.getCategory(account.categoryId)?.name?:""
            label.text = account.type.typeName + " | " + catName
        } else {
            label.text = ""
        }
    }

    private fun processAutoFill() {
        val accDebitedId = builder.accountDebitedId
        val accCreditedId = builder.accountCreditedId

        if (accDebitedId != 0 && accCreditedId != 0) {
            MoneyDAO.getTransactions()
                    .filter { it.accountCreditedId == accCreditedId && it.accountDebitedId == accDebitedId }
                    .maxWith(Transaction.BY_DATE)
                    ?.let {
                        if (commentEdit.text.isEmpty()) {
                            commentEdit.text = it.comment
                        }
                        if (sumEdit.text.isEmpty()) {
                            sumEdit.text = it.amount.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
                        }

                        MoneyDAO.getContact(it.contactId)?.let {
                            if (contactEdit.text.isEmpty()) {
                                contactEdit.text = it.name
                            }
                        }
                    }
        }
    }

    private fun autoFillType() {
        if (typeEdit.text.isEmpty()) {
            typeEdit.text = TransactionType.UNDEFINED.typeName
        }
    }

    companion object {
        private val TRANSACTION_TYPE_TO_STRING = object : ToStringConverter<TransactionType>() {
            override fun toString(obj : TransactionType): String = obj.typeName
        }

        private val CONTACT_TO_STRING = object : ToStringConverter<Contact>() {
            override fun toString(obj : Contact): String = obj.name
        }

        private val ACCOUNT_TO_STRING = object : ToStringConverter<Account>() {
            override fun toString(obj : Account): String = obj.name
        }
    }
}
