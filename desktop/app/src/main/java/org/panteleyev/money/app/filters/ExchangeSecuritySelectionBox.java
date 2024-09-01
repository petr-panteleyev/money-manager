/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.fx.ReadOnlyStringConverter;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.util.Objects;

import static org.panteleyev.money.app.Comparators.exchangeSecurityByShortName;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.util.StringUtil.decapitalize;

public class ExchangeSecuritySelectionBox extends HBox {
    private final ChoiceBox<Object> securityTypeChoiceBox = new ChoiceBox<>();
    private final ChoiceBox<Object> securityChoiceBox = new ChoiceBox<>();

    private final PredicateProperty<InvestmentDeal> investmentDealPredicateProperty = new PredicateProperty<>();

    public ExchangeSecuritySelectionBox() {
        super(BIG_SPACING);
        setAlignment(Pos.CENTER_LEFT);

        securityChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                return switch (object) {
                    case ExchangeSecurity security -> security.shortName();
                    case String string -> string;
                    case null, default -> "";
                };
            }
        });

        securityTypeChoiceBox.setOnAction(_ -> onTypeSelected());
        securityChoiceBox.setOnAction(_ -> onSecuritySelected());

        setupSecurityTypeChoiceBox();

        getChildren().addAll(securityTypeChoiceBox, securityChoiceBox);
    }

    public PredicateProperty<InvestmentDeal> investmentDealPredicateProperty() {
        return investmentDealPredicateProperty;
    }

    private void setupSecurityTypeChoiceBox() {
        securityTypeChoiceBox.getItems().add("Все типы");
        securityTypeChoiceBox.getItems().add(new Separator());
        securityTypeChoiceBox.getItems().addAll(cache().getExchangeSecurities().stream()
                .map(ExchangeSecurity::groupName)
                .distinct()
                .sorted()
                .toList());

        securityTypeChoiceBox.getSelectionModel().selectFirst();
    }

    private void setupSecurityChoiceBox() {
        securityChoiceBox.getItems().clear();
//        securityChoiceBox.getItems().add("Все бумаги");
//        securityChoiceBox.getItems().add(new Separator());

        var selectedTypeIndex = securityTypeChoiceBox.getSelectionModel().getSelectedIndex();
        if (selectedTypeIndex == 0) {
            securityChoiceBox.getItems().add("Все бумаги");
            securityChoiceBox.getItems().add(new Separator());
            securityChoiceBox.getItems().addAll(cache().getExchangeSecurities().stream()
                    .sorted(exchangeSecurityByShortName())
                    .toList());
        } else {
            var selectedGroup = securityTypeChoiceBox.getSelectionModel().getSelectedItem().toString();
            securityChoiceBox.getItems().add("Все " + decapitalize(selectedGroup));
            securityChoiceBox.getItems().add(new Separator());
            securityChoiceBox.getItems().addAll(cache().getExchangeSecurities().stream()
                    .filter(security -> Objects.equals(security.groupName(), selectedGroup))
                    .sorted(exchangeSecurityByShortName())
                    .toList());
        }

        securityChoiceBox.getSelectionModel().selectFirst();
    }

    private void onTypeSelected() {
        setupSecurityChoiceBox();
    }

    private void onSecuritySelected() {
        var selectedGroupIndex = securityTypeChoiceBox.getSelectionModel().getSelectedIndex();
        var selectedGroup = securityTypeChoiceBox.getSelectionModel().getSelectedItem();
        var selectedSecurityIndex = securityChoiceBox.getSelectionModel().getSelectedIndex();
        var selectedSecurity = securityChoiceBox.getSelectionModel().getSelectedItem();

        if (selectedGroupIndex == 0 && selectedSecurityIndex == 0) {
            investmentDealPredicateProperty.set(_ -> true);
        } else {
            if (selectedGroupIndex != 0 && selectedSecurityIndex == 0) {
                var securitiesByGroup = cache().getExchangeSecurities().stream()
                        .filter(security -> Objects.equals(security.groupName(), selectedGroup))
                        .map(ExchangeSecurity::uuid)
                        .toList();
                investmentDealPredicateProperty.set(it -> securitiesByGroup.contains(it.securityUuid()));
            } else if (selectedSecurity instanceof ExchangeSecurity security) {
                investmentDealPredicateProperty.set(
                        it -> Objects.equals(it.securityUuid(), security.uuid())
                );
            }
        }
    }
}
