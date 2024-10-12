/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.card;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
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

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.MenuFactory.checkMenuItem;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Predicates.activeCard;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_H;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

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

        var menuBar = menuBar(
                menu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                menu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(searchAction(this::onSearch))
                ),
                menu("Вид",
                        checkMenuItem("Показывать неактивные карты",
                                settings().getShowDeactivatedCards(), SHORTCUT_H,
                                event -> {
                                    var selected = ((CheckMenuItem) event.getSource()).isSelected();
                                    settings().update(opt -> opt.setShowDeactivatedCards(selected));
                                    showDeactivatedCards.set(selected ? _ -> true : activeCard(true));
                                }
                        ),
                        menuItem("Сбросить фильтр", SHORTCUT_ALT_C, _ -> resetFilter())),
                createWindowMenu(),
                createHelpMenu()
        );

        // Context Menu
        tableView.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        var pane = new BorderPane(tableView,
                fxNode(
                        hBox(List.of(cardNumberFilterBox.getTextField() /*, typeBox*/), b -> {
                            b.setSpacing(BIG_SPACING);
                            b.setAlignment(Pos.CENTER_LEFT);
                        }),
                        b -> BorderPane.setMargin(b, BIG_INSETS)
                ),
                null, null, null);

        var self = new BorderPane(pane, menuBar, null, null, null);
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
}
