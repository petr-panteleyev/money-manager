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

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class AccountListWindowController extends BaseController {
    private BorderPane self = new BorderPane();

    private ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();
    private CheckBox showActiveCheckBox = new CheckBox(RB.getString("account.Window.ShowOnlyActive"));
    private TableView<Account> accountListTable = new TableView<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<Integer, Account> accountsListener =
            change -> Platform.runLater(this::reloadAccounts);

    AccountListWindowController() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = event -> onAddAccount();
        EventHandler<ActionEvent> editHandler = event -> onEditAccount();
        EventHandler<ActionEvent> deleteHandler = event -> onDeleteAccount();

        // Main menu
        MenuItem closeMenuItem = new MenuItem(RB.getString("menu.File.Close"));
        closeMenuItem.setOnAction(event -> onClose());
        Menu fileMenu = new Menu(RB.getString("menu.File"), null, closeMenuItem);

        MenuItem addMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        addMenuItem.setOnAction(addHandler);
        MenuItem editMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        editMenuItem.setOnAction(editHandler);
        MenuItem deleteMenuItem = new MenuItem(RB.getString("menu.Edit.Delete"));
        deleteMenuItem.setOnAction(deleteHandler);
        Menu editMenu = new Menu(RB.getString("menu.Edit"), null,
                addMenuItem, editMenuItem, new SeparatorMenuItem(), deleteMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, createHelpMenu(RB));
        menuBar.setUseSystemMenuBar(true);

        // Context menu
        MenuItem ctxAddMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);
        MenuItem ctxEditMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);
        MenuItem ctxDeleteMenuItem = new MenuItem(RB.getString("menu.Edit.Delete"));
        ctxDeleteMenuItem.setOnAction(deleteHandler);
        accountListTable.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem, new SeparatorMenuItem(), ctxDeleteMenuItem));

        // Table
        TableColumn<Account, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Account, String> nameColumn = new TableColumn<>(RB.getString("column.Name"));
        TableColumn<Account, String> typeColumn = new TableColumn<>(RB.getString("column.Type"));
        TableColumn<Account, String> categoryColumn = new TableColumn<>(RB.getString("column.Category"));
        TableColumn<Account, String> currencyColumn = new TableColumn<>(RB.getString("column.Currency"));
        TableColumn<Account, BigDecimal> balanceColumn = new TableColumn<>(RB.getString("column.InitialBalance"));
        TableColumn<Account, CheckBox> activeColumn = new TableColumn<>("A");

        accountListTable.getColumns().setAll(Arrays.asList(
                idColumn, nameColumn, typeColumn, categoryColumn,
                currencyColumn, balanceColumn, activeColumn));

        // Content
        BorderPane pane = new BorderPane();

        // Toolbox
        HBox hBox = new HBox(typeChoiceBox, categoryChoiceBox, showActiveCheckBox);
        hBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(categoryChoiceBox, new Insets(0.0, 0.0, 0.0, 5.0));
        HBox.setMargin(showActiveCheckBox, new Insets(0.0, 0.0, 0.0, 10.0));

        pane.setTop(hBox);
        pane.setCenter(accountListTable);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        self.setPrefSize(800.0, 400.0);
        self.setTop(menuBar);
        self.setCenter(pane);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ObservableList<Object> types = FXCollections.observableArrayList(CategoryType.values());
        if (!types.isEmpty()) {
            types.add(0, new Separator());
        }
        types.add(0, RB.getString("account.Window.AllTypes"));

        typeChoiceBox.setItems(types);
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof CategoryType) {
                    return ((CategoryType) object).getTypeName();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> onTypeChanged(newValue));

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Category) {
                    return ((Category) object).getName();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });

        categoryChoiceBox.setItems(FXCollections.observableArrayList(RB.getString("account.Window.AllCategories")));
        categoryChoiceBox.getSelectionModel().select(0);
        categoryChoiceBox.valueProperty().addListener((x, y, z) -> reloadAccounts());

        showActiveCheckBox.setOnAction(event -> reloadAccounts());

        idColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Integer> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getId()));
        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getName()));

        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getType().getTypeName()));
        categoryColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper<>(getDao().getCategory(p.getValue().getCategoryId()).map(Category::getName).orElse("")));
        currencyColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper<>(getDao().getCurrency(p.getValue().getCurrencyId()).map(Currency::getSymbol).orElse("")));
        balanceColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, BigDecimal> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getOpeningBalance().setScale(2, RoundingMode.HALF_UP)));
        activeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, CheckBox> p) -> {
            Account account = p.getValue();

            CheckBox cb = new CheckBox();
            cb.setSelected(account.getEnabled());

            cb.setOnAction(event -> {
                boolean enabled = account.getEnabled();
                getDao().updateAccount(account.enable(!enabled));
            });

            return new ReadOnlyObjectWrapper<>(cb);
        });

        editMenuItem.disableProperty()
                .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        deleteMenuItem.disableProperty()
                .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
                .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        ctxDeleteMenuItem.disableProperty()
                .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());

        reloadAccounts();

        getDao().accounts().addListener(new WeakMapChangeListener<>(accountsListener));

        setupWindow(self);
    }

    public String getTitle() {
        return RB.getString("account.Window.Title");
    }

    private void reloadAccounts() {
        Collection<Account> accounts;

        Object catObject = categoryChoiceBox.getSelectionModel().getSelectedItem();
        if (catObject instanceof Category) {
            accounts = getDao().getAccountsByCategory(((Category) catObject).getId());
        } else {
            Object typeObject = typeChoiceBox.getSelectionModel().getSelectedItem();
            if (typeObject instanceof CategoryType) {
                accounts = getDao().getAccountsByType((CategoryType) typeObject);
            } else {
                accounts = getDao().getAccounts();
            }
        }

        if (showActiveCheckBox.isSelected()) {
            accounts = accounts.stream()
                    .filter(Account::getEnabled)
                    .collect(Collectors.toList());
        }

        accountListTable.setItems(FXCollections.observableArrayList(accounts));
    }

    private void onTypeChanged(Object newValue) {
        ObservableList<Object> items;

        if (newValue instanceof String) {
            items = FXCollections.observableArrayList(RB.getString("account.Window.AllCategories"));
        } else {
            items = FXCollections.observableArrayList(getDao().getCategoriesByType((CategoryType) newValue));

            if (!items.isEmpty()) {
                items.add(0, new Separator());
            }
            items.add(0, RB.getString("account.Window.AllCategories"));
        }

        categoryChoiceBox.setItems(items);
        categoryChoiceBox.getSelectionModel().select(0);
    }

    private void onAddAccount() {
        new AccountDialog(null, null).showAndWait()
                .ifPresent(a -> getDao().insertAccount(a.copy(getDao().generatePrimaryKey(Account.class)))
                );
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.of(accountListTable.getSelectionModel().getSelectedItem());
    }

    private void onEditAccount() {
        getSelectedAccount().ifPresent(account ->
                new AccountDialog(account, null).showAndWait()
                        .ifPresent(it -> getDao().updateAccount(it)));
    }

    private void onDeleteAccount() {
        getSelectedAccount().ifPresent(account -> {
            long count = getDao().getTransactionCount(account);
            if (count != 0L) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to delete account\nwith $count associated transactions",
                        ButtonType.CLOSE).showAndWait();
            } else {
                new Alert(Alert.AlertType.CONFIRMATION, RB.getString("text.AreYouSure"), ButtonType.OK, ButtonType.CANCEL)
                        .showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(b -> getDao().deleteAccount(account));
            }
        });
    }
}
