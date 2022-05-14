/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
import javafx.stage.FileChooser;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.filters.AccountSelectionBox;
import org.panteleyev.money.app.filters.ContactFilterBox;
import org.panteleyev.money.app.filters.TransactionFilterBox;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.DataCache;
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
import static org.controlsfx.control.textfield.TextFields.bindAutoCompletion;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.SKIP;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_K;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.TransactionPredicate.transactionByAccount;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_DELETE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_REPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_UNCHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_VIEW;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_RESET_FILTER;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_UNCHECKED_ONLY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DETAILS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REQUESTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;

class RequestWindowController extends BaseController {
    private final Account account;

    private final TransactionTableView table;

    private final AccountSelectionBox accBox = new AccountSelectionBox();
    private final TransactionFilterBox transactionFilterBox = new TransactionFilterBox(true, true);
    private final ContactFilterBox contactFilterBox = new ContactFilterBox();
    private final TextField sumField = new TextField();

    private final PredicateProperty<Transaction> uncheckedPredicate = new PredicateProperty<>();
    private final PredicateProperty<Transaction> filterProperty;

    private final TreeSet<String> contactSuggestions = new TreeSet<>();

    private static class CompletionProvider extends BaseCompletionProvider<String> {
        CompletionProvider(Set<String> set) {
            super(set, () -> settings().getAutoCompleteLength());
        }

        public String getElementString(String element) {
            return element;
        }
    }

    RequestWindowController(Account account) {
        this.account = account;

        table = account == null ?
            new TransactionTableView(this, TransactionTableView.Mode.QUERY) :
            new TransactionTableView(this, account);

        var uncheckedOnlyCheckBox = new CheckBox(fxString(UI, I18N_MISC_UNCHECKED_ONLY));
        uncheckedOnlyCheckBox.selectedProperty().addListener(
            (v, old, newValue) -> uncheckedPredicate.set(newValue ? t -> !t.checked() : t -> true)
        );

        filterProperty = PredicateProperty.and(List.of(
            account == null ? accBox.predicateProperty() : new PredicateProperty<>(transactionByAccount(account.uuid())),
            transactionFilterBox.predicateProperty(),
            contactFilterBox.predicateProperty(),
            uncheckedPredicate
        ));

        var filterBox = hBox(5.0,
            account == null ? accBox : SKIP,
            transactionFilterBox,
            label(fxString(UI, I18N_WORD_COUNTERPARTY, COLON)),
            contactFilterBox.getTextField(),
            uncheckedOnlyCheckBox,
            fxNode(new Region(), hBoxHGrow(ALWAYS)),
            label(fxString(UI, I18N_WORD_SUM, COLON)),
            sumField
        );

        filterBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(filterBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var centerBox = new BorderPane();
        centerBox.setTop(filterBox);
        centerBox.setCenter(table);

        sumField.setEditable(false);
        sumField.setFocusTraversable(false);

        filterProperty.addListener((x, y, newValue) -> onUpdateFilter());

        transactionFilterBox.setTransactionFilter(TransactionPredicate.CURRENT_MONTH);

        table.setOnCheckTransaction(this::onCheckTransaction);

        var root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(centerBox);

        transactionFilterBox.setFilterYears();

        bindAutoCompletion(contactFilterBox.getTextField(), new CompletionProvider(contactSuggestions));
        setupContactMenu();

        table.selectedTransactions().addListener((ListChangeListener<Transaction>) change ->
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
        return account == null ? UI.getString(I18N_WORD_REQUESTS) : account.name();
    }

    boolean thisAccount(Account account) {
        return Objects.equals(this.account, account);
    }

    private Predicate<Transaction> getTransactionFilter() {
        return table.getTransactionFilter();
    }

    private MenuBar createMenuBar() {
        var menuBar = menuBar(
            newMenu(fxString(UI, I18N_MENU_FILE),
                menuItem(fxString(UI, I18N_MENU_ITEM_REPORT, ELLIPSIS), event -> onReport()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CLOSE), event -> onClose())),
            newMenu(fxString(UI, I18N_MENU_EDIT),
                menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E, event -> table.onEditTransaction()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_DELETE, ELLIPSIS), SHORTCUT_DELETE, event -> table.onDeleteTransaction()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_WORD_DETAILS, ELLIPSIS), event -> table.onTransactionDetails()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CHECK), SHORTCUT_K, event -> table.onCheckTransactions(true)),
                menuItem(fxString(UI, I18N_MENU_ITEM_UNCHECK), SHORTCUT_U, event -> table.onCheckTransactions(false))
            ),
            newMenu(fxString(UI, I18N_MENU_VIEW),
                menuItem(fxString(UI, I18N_MISC_RESET_FILTER), SHORTCUT_ALT_C, event -> resetFilter())),
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
        var fileChooser = new FileChooser();
        fileChooser.setTitle(fxString(UI, I18N_WORD_REPORT));
        settings().getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("transactions"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(getStage());
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            var transactions = cache().getTransactions(getTransactionFilter())
                .sorted(cache().getTransactionByDateComparator())
                .toList();
            Reports.reportTransactions(transactions, outputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void setupContactMenu() {
        contactSuggestions.clear();
        cache().getContacts().stream()
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .map(Contact::name)
            .forEach(contactSuggestions::add);
    }
}
