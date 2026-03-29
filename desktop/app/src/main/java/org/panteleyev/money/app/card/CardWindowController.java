// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.card;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.filters.CardNumberFilterBox;
import org.panteleyev.money.model.Card;

import java.util.List;
import java.util.Optional;

import static org.panteleyev.fx.factories.BoxFactory.hBox;
import static org.panteleyev.fx.factories.MenuFactory.checkMenuItem;
import static org.panteleyev.fx.factories.MenuFactory.menu;
import static org.panteleyev.fx.factories.MenuFactory.menuBar;
import static org.panteleyev.fx.factories.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Predicates.activeCard;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_H;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.Styles.BIG_SPACING;

public final class CardWindowController extends BaseController {
    private final CardNumberFilterBox cardNumberFilterBox = new CardNumberFilterBox();
    private final PredicateProperty<Card> showDeactivatedCards =
            new PredicateProperty<>(settings().getShowDeactivatedCards() ? _ -> true : activeCard(true));

    private final PredicateProperty<Card> filterProperty =
            PredicateProperty.and(List.of(
                    cardNumberFilterBox.predicateProperty(),
                    showDeactivatedCards
            ));

    private final FilteredList<Card> filteredList = cache().getCards().filtered(filterProperty.get());
    private final TableView<Card> tableView = new CardTableView(filteredList.sorted());

    public CardWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCard, this::onEditCard, _ -> {},
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );

        // Context Menu
        tableView.setContextMenu(new ContextMenu(
                crudActionsHolder.getCreateAction().createMenuItem(),
                crudActionsHolder.getUpdateAction().createMenuItem()
        ));

        var toolBar = hBox(List.of(cardNumberFilterBox.getTextField()) /*, typeBox*/);
        toolBar.setSpacing(BIG_SPACING);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(toolBar, BIG_INSETS);

        var pane = new BorderPane(tableView, toolBar, null, null, null);
        var self = new BorderPane(pane, createMenuBar(crudActionsHolder), null, null, null);
        self.setPrefSize(600.0, 400.0);

        filteredList.predicateProperty().bind(filterProperty);

        setupWindow(self);
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Card> getSelectedCard() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return "Карты";
    }

    private MenuBar createMenuBar(CrudActionsHolder crudActionsHolder) {
        var showInactiveCardsMenuItem = checkMenuItem("Показывать неактивные карты");
        showInactiveCardsMenuItem.setSelected(settings().getShowDeactivatedCards());
        showInactiveCardsMenuItem.setAccelerator(SHORTCUT_H);
        showInactiveCardsMenuItem.setOnAction(this::onShowDeactivatedCards);
        var resetFilterMenuItem = menuItem("Сбросить фильтр", _ -> resetFilter());
        resetFilterMenuItem.setAccelerator(SHORTCUT_ALT_C);

        return menuBar(
                menu("Файл", ACTION_CLOSE.createMenuItem()),
                menu("Правка",
                        crudActionsHolder.getCreateAction().createMenuItem(),
                        crudActionsHolder.getUpdateAction().createMenuItem(),
                        new SeparatorMenuItem(),
                        searchAction(this::onSearch).createMenuItem()
                ),
                menu("Вид", showInactiveCardsMenuItem, resetFilterMenuItem),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private void resetFilter() {
        cardNumberFilterBox.getTextField().setText("");
    }

    private void onCreateCard(ActionEvent ignored) {
        new CardDialog(this, null).showAndWait()
                .ifPresent(card -> dao().insertCard(card));
    }

    private void onEditCard(ActionEvent ignored) {
        getSelectedCard().flatMap(selected -> new CardDialog(this, selected).showAndWait())
                .ifPresent(card -> dao().updateCard(card));
    }

    private void onSearch(ActionEvent ignored) {
        cardNumberFilterBox.getTextField().requestFocus();
    }

    private void onShowDeactivatedCards(ActionEvent event) {
        if (event.getSource() instanceof CheckMenuItem checkMenuItem) {
            var selected = checkMenuItem.isSelected();
            settings().update(opt -> opt.setShowDeactivatedCards(selected));
            showDeactivatedCards.set(selected ? _ -> true : activeCard(true));
        }
    }
}
