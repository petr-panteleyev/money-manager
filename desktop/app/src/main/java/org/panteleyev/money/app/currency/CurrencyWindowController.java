// Copyright © 2020-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.currency;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Currency;

import java.util.Optional;

import static org.panteleyev.fx.factories.MenuFactory.menu;
import static org.panteleyev.fx.factories.MenuFactory.menuBar;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;

public final class CurrencyWindowController extends BaseController {
    private final TableView<Currency> tableView = new CurrencyTableView(cache().getCurrencies().sorted());

    public CurrencyWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCurrency, this::onEditCurrency, this::onDeleteCurrency,
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );

        // Context Menu
        tableView.setContextMenu(new ContextMenu(
                crudActionsHolder.getCreateAction().createMenuItem(),
                crudActionsHolder.getUpdateAction().createMenuItem(),
                new SeparatorMenuItem(),
                crudActionsHolder.getDeleteAction().createMenuItem()
        ));

        var root = new BorderPane(
                new BorderPane(tableView, null, null, null, null),
                createMenuBar(crudActionsHolder), null, null, null
        );

        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Валюты";
    }

    private MenuBar createMenuBar(CrudActionsHolder crudActionsHolder) {
        return menuBar(
                menu("Файл", ACTION_CLOSE.createMenuItem()),
                menu("Правка",
                        crudActionsHolder.getCreateAction().createMenuItem(),
                        crudActionsHolder.getUpdateAction().createMenuItem(),
                        new SeparatorMenuItem(),
                        crudActionsHolder.getDeleteAction().createMenuItem()
                ),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void onCreateCurrency(ActionEvent event) {
        new CurrencyDialog(this, settings().getDialogCssFileUrl(), null)
                .showAndWait()
                .ifPresent(c -> dao().insertCurrency(c));
    }

    private void onEditCurrency(ActionEvent event) {
        getSelectedCurrency().flatMap(selected ->
                        new CurrencyDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
                .ifPresent(c -> dao().updateCurrency(c));
    }

    private void onDeleteCurrency(ActionEvent ignored) {
        getSelectedCurrency().ifPresent(currency ->
                new Alert(Alert.AlertType.CONFIRMATION, "Удалить эту валюту?")
                        .showAndWait()
                        .ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                dao().deleteCurrency(currency);
                            }
                        }));
    }
}
