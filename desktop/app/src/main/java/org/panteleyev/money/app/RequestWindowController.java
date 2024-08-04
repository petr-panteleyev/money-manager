/*
 Copyright © 2019-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.dialogs.ReportFileDialog;
import org.panteleyev.money.app.filters.AccountSelectionBox;
import org.panteleyev.money.app.filters.ContactFilterBox;
import org.panteleyev.money.app.filters.TransactionFilterBox;
import org.panteleyev.money.app.transaction.TransactionPredicate;
import org.panteleyev.money.app.transaction.TransactionTableView;
import org.panteleyev.money.app.util.StringCompletionProvider;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import static javafx.scene.layout.Priority.ALWAYS;
import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.controlsfx.control.textfield.TextFields.bindAutoCompletion;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.SKIP;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_R;
import static org.panteleyev.money.app.transaction.TransactionPredicate.transactionByAccount;

class RequestWindowController extends BaseController {
    private final Account account;

    private final TransactionTableView table;

    private final AccountSelectionBox accBox = new AccountSelectionBox();
    private final TransactionFilterBox transactionFilterBox = new TransactionFilterBox(true, true);
    private final ContactFilterBox contactFilterBox = new ContactFilterBox();
    private final TextField sumField = new TextField();

    private final PredicateProperty<Transaction> uncheckedPredicate = new PredicateProperty<>();
    private final PredicateProperty<Transaction> filterProperty;

    private final Set<String> contactSuggestions = new TreeSet<>();

    RequestWindowController(Account account) {
        this.account = account;

        table = account == null ?
                new TransactionTableView(this, TransactionTableView.Mode.QUERY) :
                new TransactionTableView(this, account);

        var uncheckedOnlyCheckBox = new CheckBox("Только неотмеченные");
        uncheckedOnlyCheckBox.selectedProperty().addListener(
                (_, _, newValue) -> uncheckedPredicate.set(newValue ? t -> !t.checked() : _ -> true)
        );

        filterProperty = PredicateProperty.and(List.of(
                account == null ? accBox.predicateProperty() :
                        new PredicateProperty<>(transactionByAccount(account.uuid())),
                transactionFilterBox.predicateProperty(),
                contactFilterBox.predicateProperty(),
                uncheckedPredicate
        ));

        var filterBox = hBox(5.0,
                account == null ? accBox : SKIP,
                transactionFilterBox,
                label("Контрагент:"),
                contactFilterBox.getTextField(),
                uncheckedOnlyCheckBox,
                fxNode(new Region(), hBoxHGrow(ALWAYS)),
                label("Сумма:"),
                sumField
        );

        filterBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(filterBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var centerBox = new BorderPane();
        centerBox.setTop(filterBox);
        centerBox.setCenter(table);

        sumField.setEditable(false);
        sumField.setFocusTraversable(false);

        filterProperty.addListener((_, _, _) -> onUpdateFilter());

        transactionFilterBox.setTransactionFilter(TransactionPredicate.CURRENT_MONTH);

        table.setOnCheckTransaction(this::onCheckTransaction);

        var root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(centerBox);

        transactionFilterBox.setFilterYears();

        bindAutoCompletion(contactFilterBox.getTextField(), new StringCompletionProvider(contactSuggestions));
        setupContactMenu();

        table.selectedTransactions().addListener((ListChangeListener<Transaction>) _ ->
                sumField.setText(
                        DataCache.calculateBalance(table.selectedTransactions()).setScale(2, RoundingMode.HALF_UP).toString()
                ));

        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    Account getAccount() {
        return account;
    }

    @Override
    public String getTitle() {
        return account == null ? "Запросы" : account.name();
    }

    boolean thisAccount(Account account) {
        return Objects.equals(this.account, account);
    }

    private Predicate<Transaction> getTransactionFilter() {
        return table.getTransactionFilter();
    }

    private MenuBar createMenuBar() {
        var menuBar = menuBar(
                menu("Файл",
                        menuItem("Отчет...", SHORTCUT_ALT_R, _ -> onReport()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)
                ),
                createMenu("Правка", table.getActions()),
                menu("Вид",
                        menuItem("Сбросить фильтр", SHORTCUT_ALT_C, _ -> resetFilter())),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));
        return menuBar;
    }

    private void onUpdateFilter() {
        table.setTransactionFilter(filterProperty.get());
    }

    private void resetFilter() {
        accBox.reset();
        contactFilterBox.getTextField().clear();
        transactionFilterBox.setTransactionFilter(TransactionPredicate.CURRENT_MONTH);
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        dao().checkTransactions(transactions, check);
    }

    private void onReport() {
        new ReportFileDialog().show(getStage(), ReportType.TRANSACTIONS).ifPresent(selected -> {
            try (var outputStream = new FileOutputStream(selected)) {
                var transactions = cache().getTransactions(getTransactionFilter())
                        .sorted(Comparators.transactionsByDate())
                        .toList();
                Reports.reportTransactions(transactions, outputStream);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void setupContactMenu() {
        contactSuggestions.clear();
        cache().getContacts().stream()
                .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
                .map(Contact::name)
                .forEach(contactSuggestions::add);
    }
}
