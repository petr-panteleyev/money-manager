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
import javafx.geometry.Insets
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ProgressBar
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.MySQLBuilder
import org.panteleyev.money.xml.Export
import org.panteleyev.utilities.fx.Controller
import org.panteleyev.utilities.fx.WindowManager
import java.io.FileOutputStream
import java.math.BigDecimal
import java.util.Arrays
import java.util.ResourceBundle
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

class MainWindowController(stage: Stage) : BaseController(stage, MainWindowController.CSS_PATH) {

    private val rb = ResourceBundle.getBundle(UI_BUNDLE_PATH)

    private val self = BorderPane()
    private val tabPane = TabPane()

    private val progressLabel = Label()
    private val progressBar = ProgressBar()

    private val windowMenu = Menu(rb.getString("menu.Window"))

    private val accountsTab = AccountsTab()
    private val transactionTab = TransactionsTab()
    private val requestTab = RequestTab()

    private val dbOpenProperty = SimpleBooleanProperty(false)

    init {
        stage.icons.add(Images.APP_ICON)
        initialize()
        setupWindow(self)
    }

    override fun getTitle(): String {
        return "Money Manager"
    }

    private fun initialize() {
        // Main menu
        val m1 = MenuItem(rb.getString("menu.File.New"))
        m1.setOnAction { onNewConnection() }
        val m2 = MenuItem(rb.getString("menu.File.Open"))
        m2.setOnAction { onOpenConnection() }
        val m3 = MenuItem(rb.getString("menu.File.Close"))
        m3.setOnAction { onClose() }
        val m4 = MenuItem(rb.getString("menu.File.Exit"))
        m4.setOnAction { onExit() }

        val fileMenu = Menu(rb.getString("menu.File"), null,
                m1, m2, SeparatorMenuItem(), m3, SeparatorMenuItem(), m4)

        val m5 = MenuItem(rb.getString("menu.Edit.Delete"))

        val currenciesMenuItem = MenuItem(rb.getString("menu.Edit.Currencies"))
        currenciesMenuItem.setOnAction { onManageCurrencies() }
        val categoriesMenuItem = MenuItem(rb.getString("menu.Edit.Categories"))
        categoriesMenuItem.setOnAction { onManageCategories() }
        val accountsMenuItem = MenuItem(rb.getString("menu.Edit.Accounts"))
        accountsMenuItem.setOnAction { onManageAccounts() }
        val contactsMenuItem = MenuItem(rb.getString("menu.Edit.Contacts"))
        contactsMenuItem.setOnAction { onManageContacts() }

        val editMenu = Menu(rb.getString("menu.Edit"), null,
                m5, SeparatorMenuItem(),
                currenciesMenuItem, categoriesMenuItem, accountsMenuItem, contactsMenuItem)

        val dumpXmlMenuItem = MenuItem(rb.getString("menu.Tools.Export")).apply {
            setOnAction { xmlDump() }
        }

        val importMenuItem = MenuItem(RB.getString("word.Import") + "...").apply {
            setOnAction { onImport() }
        }

        val m7 = MenuItem(rb.getString("menu.Tools.Options"))
        m7.setOnAction { onOptions() }

        val toolsMenu = Menu(rb.getString("menu.Tools"), null,
                dumpXmlMenuItem, importMenuItem, SeparatorMenuItem(), m7)

        /* Dummy menu item is required in order to let onShowing() fire up first time */
        windowMenu.items.setAll(MenuItem("dummy"))

        val menuBar = MenuBar(fileMenu, editMenu, toolsMenu,
                windowMenu, createHelpMenu(rb))

        menuBar.isUseSystemMenuBar = true

        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        self.top = menuBar
        self.center = tabPane
        self.bottom = HBox(progressLabel, progressBar)

        HBox.setMargin(progressLabel, Insets(0.0, 0.0, 0.0, 5.0))
        HBox.setMargin(progressBar, Insets(0.0, 0.0, 0.0, 5.0))

        progressLabel.isVisible = false
        progressBar.isVisible = false

        currenciesMenuItem.disableProperty().bind(dbOpenProperty.not())
        categoriesMenuItem.disableProperty().bind(dbOpenProperty.not())
        accountsMenuItem.disableProperty().bind(dbOpenProperty.not())
        contactsMenuItem.disableProperty().bind(dbOpenProperty.not())

        dumpXmlMenuItem.disableProperty().bind(dbOpenProperty.not())
        importMenuItem.disableProperty().bind(dbOpenProperty.not())

        val t2 = Tab(rb.getString("tab.Transactions"), transactionTab)
        t2.disableProperty().bind(dbOpenProperty.not())
        t2.selectedProperty().addListener { _, _, newValue ->
            if (newValue!!) {
                Platform.runLater { transactionTab.transactionEditor.clear() }
                Platform.runLater { transactionTab.scrollToEnd() }
            }
        }

        val t3 = Tab(rb.getString("tab.Requests"), requestTab)
        t3.disableProperty().bind(dbOpenProperty.not())

        tabPane.tabs.addAll(
                Tab(rb.getString("tab.Accouts"), accountsTab),
                t2, t3
        )

        windowMenu.setOnShowing {
            windowMenu.items.clear()

            windowMenu.items.add(MenuItem("Money Manager"))
            windowMenu.items.add(SeparatorMenuItem())

            WindowManager.getFrames().forEach { c ->
                val item = MenuItem(c.title)
                item.setOnAction { c.stage.toFront() }
                windowMenu.items.add(item)
            }
        }

        stage.setOnHiding { onWindowClosing() }

        stage.width = Options.mainWindowWidth
        stage.height = Options.mainWindowHeight

        /*
         * Application parameters:
         * --host=<host>
         * --port=<port>
         * --user=<user>
         * --password=<password>
         * --name=<name>
         */
        val params = MoneyApplication.application!!.parameters
        val name: String? = params.named["name"]
        if (name != null) {
            // check mandatory parameters
            val host = params.named.getOrDefault("host", "localhost")
            val port = Integer.parseInt(params.named.getOrDefault("port", "3306"))
            val user = params.named["user"]
            val password = params.named.getOrDefault("password", "")

            if (user == null) {
                throw IllegalArgumentException("User name cannot be empty")
            }

            val builder = MySQLBuilder()
                    .host(host)
                    .port(port)
                    .user(user)
                    .password(password)
                    .name(name)
            open(builder, false)
        } else {
            if (Options.autoConnect) {
                val builder = MySQLBuilder()
                        .host(Options.databaseHost)
                        .port(Options.databasePort)
                        .user(Options.databaseUser)
                        .password(Options.databasePassword)
                        .name(Options.databaseName)
                open(builder, false)
            }
        }
    }

    private fun onManageCategories() {
        val controller = WindowManager.find(CategoryWindowController::class.java)
                .orElseGet({ CategoryWindowController() })

        val stage = controller.stage
        stage.show()
        stage.toFront()
    }

    private fun onExit() {
        stage.fireEvent(WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST))
    }

    private fun onManageCurrencies() {
        val controller = WindowManager.find(CurrencyWindowController::class.java)
                .orElseGet({ CurrencyWindowController() })

        val stage = controller.stage
        stage.show()
        stage.toFront()
    }

    private fun onManageAccounts() {
        val controller = WindowManager.find(AccountListWindowController::class.java)
                .orElseGet({ AccountListWindowController() })

        val stage = controller.stage
        stage.show()
        stage.toFront()
    }

    private fun onManageContacts() {
        val controller = WindowManager.find(ContactListWindowController::class.java)
                .orElseGet({ ContactListWindowController() })

        val stage = controller.stage
        stage.show()
        stage.toFront()
    }

    private fun onNewConnection() {
        ConnectionDialog(true).showAndWait().ifPresent { builder ->
            val ds = builder.build()

            val newResult = CompletableFuture.runAsync {
                MoneyDAO.initialize(ds)
                MoneyDAO.createTables()
                MoneyDAO.preload()
            }.thenRun {
                Platform.runLater {
                    setTitle(AboutDialog.APP_TITLE + " - " + builder.connectionString())
                    dbOpenProperty.set(true)
                }
            }

            checkFutureException(newResult)
        }
    }

    override fun onClose() {
        WINDOW_CLASSES.forEach { clazz -> WindowManager.find(clazz).ifPresent { c -> (c as BaseController).onClose() } }

        tabPane.selectionModel.select(0)

        setTitle(AboutDialog.APP_TITLE)
        MoneyDAO.initialize(null)
        dbOpenProperty.set(false)
    }

    private fun onOpenConnection() {
        ConnectionDialog(false).showAndWait().ifPresent { builder -> open(builder, true) }
    }

    private fun open(builder: MySQLBuilder, overwriteOption: Boolean) {
        if (overwriteOption) {
            Options.databaseHost = builder.host
            Options.databasePort = builder.port
            Options.databaseUser = builder.user
            Options.databasePassword = builder.password
            Options.databaseName = builder.dbName
        }

        val ds = builder.build()

        MoneyDAO.initialize(ds)

        val loadResult = CompletableFuture
                .runAsync { MoneyDAO.preload() }
                .thenRun {
                    Platform.runLater {
                        setTitle(AboutDialog.APP_TITLE + " - " + builder.connectionString())
                        dbOpenProperty.set(true)
                    }
                }

        checkFutureException(loadResult)
    }

    private fun checkFutureException(f: Future<*>) {
        try {
            f.get()
        } catch (ex: ExecutionException) {
            MoneyApplication.uncaughtException(ex.cause!!)
        } catch (ex: InterruptedException) {
            MoneyApplication.uncaughtException(ex)
        }
    }

    private fun onOptions() {
        val d = OptionsDialog()
        d.showAndWait()
    }

    private fun setTitle(title: String) {
        stage.title = title
    }

    private fun onWindowClosing() {
        WINDOW_CLASSES.forEach { clazz -> WindowManager.find(clazz).ifPresent { c -> (c as BaseController).onClose() } }

        Options.mainWindowWidth = stage.widthProperty().doubleValue()
        Options.mainWindowHeight = stage.heightProperty().doubleValue()
    }

    private fun xmlDump() {
        val selected = FileChooser().apply {
            title = "Export to file"
            extensionFilters.addAll(
                    FileChooser.ExtensionFilter("XML Files", "*.xml"),
                    FileChooser.ExtensionFilter("All Files", "*.*")
            )
        }.showSaveDialog(null)

        selected?.let {
            CompletableFuture.runAsync {
                FileOutputStream(selected).use {
                    Export()
                            .withCategories(MoneyDAO.getCategories())
                            .withAccounts(MoneyDAO.getAccounts())
                            .withCurrencies(MoneyDAO.getCurrencies())
                            .withContacts(MoneyDAO.getContacts())
                            .withTransactionGroups(MoneyDAO.getTransactionGroups())
                            .withTransactions(MoneyDAO.getTransactions())
                            .export(it)
                }
            }
        }
    }

    private fun onImport() {
        ImportWizard().showAndWait()
    }

    companion object {
        val UI_BUNDLE_PATH = "org.panteleyev.money.res.ui"
        val CSS_PATH = "/org/panteleyev/money/res/main.css"

        val RB = ResourceBundle.getBundle(UI_BUNDLE_PATH)

        private val WINDOW_CLASSES = Arrays.asList<Class<out Controller>>(
                ContactListWindowController::class.java,
                AccountListWindowController::class.java,
                CategoryWindowController::class.java,
                CurrencyWindowController::class.java
        )

        internal val BIG_DECIMAL_VALIDATOR = { control: Control, value: String ->
            var invalid = false
            try {
                BigDecimal(value)
            } catch (ex: NumberFormatException) {
                invalid = true
            }

            ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled)
        }
    }
}
