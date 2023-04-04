/*
 Copyright © 2020-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Currency;

import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;

final class CurrencyWindowController extends BaseController {

    private final TableView<Currency> table = new TableView<>(cache().getCurrencies());

    CurrencyWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCurrency, this::onEditCurrency, e -> {},
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getDeleteAction())
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createMenuItem(crudActionsHolder.getCreateAction()),
                createMenuItem(crudActionsHolder.getDeleteAction())
        ));

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn("Название", b ->
                        b.withPropertyCallback(Currency::symbol).withWidthBinding(w.multiply(0.2))),
                tableColumn("Описание", b ->
                        b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));

        var root = new BorderPane(table, menuBar, null, null, null);
        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Валюты";
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void onCreateCurrency(ActionEvent event) {
        new CurrencyDialog(this, settings().getDialogCssFileUrl(), null)
                .showAndWait()
                .ifPresent(c -> dao().insertCurrency(c));
    }

    private void onEditCurrency(ActionEvent event) {
        getSelectedCurrency()
                .flatMap(selected ->
                        new CurrencyDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
                .ifPresent(c -> dao().updateCurrency(c));
    }
}
