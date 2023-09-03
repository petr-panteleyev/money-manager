/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.exchange;

import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.panteleyev.fx.FxFactory.textField;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;

public class ExchangeSecurityDialog extends BaseDialog<Object> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ExchangeSecurityDialog(Controller owner, ExchangeSecurity security) {
        super(owner, settings().getDialogCssFileUrl());

        Objects.requireNonNull(security);
        setTitle(security.secId());

        var grid = new GridPane();
        var rowIndex = new AtomicInteger(0);

        addGridRow(grid, rowIndex, "Код:", security.secId());
        addGridRow(grid, rowIndex, "Тип:", security.typeName());
        addGridRow(grid, rowIndex, "Краткое наименование:", security.shortName());
        addGridRow(grid, rowIndex, "Полное наименование:", security.name());
        addGridRow(grid, rowIndex, "ISIN:", security.isin());
        addGridRow(grid, rowIndex, "Номер гос. регистрации:", security.regNumber());
        addGridRow(grid, rowIndex, "Номинальная стоимость:", security.faceValue());
        addGridRow(grid, rowIndex, "Дата начала торгов:", security.issueDate());
        addGridRow(grid, rowIndex, "Рыночная стоимость:", security.marketValue());

        security.getCouponValue().ifPresent(value ->
                addGridRow(grid, rowIndex, "Сумма купона:", value));
        security.getCouponPercent().ifPresent(value ->
                addGridRow(grid, rowIndex, "Ставка купона, %:", value));
        security.getCouponDate().ifPresent(value ->
                addGridRow(grid, rowIndex, "Дата выплаты купона:", value));
        security.getAccruedInterest().ifPresent(value ->
                addGridRow(grid, rowIndex, "НКД:", value));
        security.getMatDate().ifPresent(value ->
                addGridRow(grid, rowIndex, "Дата погашения:", value));
        security.getDaysToRedemption().ifPresent(value ->
                addGridRow(grid, rowIndex, "Дней до погашения:", value));

        setResultConverter((ButtonType b) -> b == ButtonType.OK ? true : null);

        getDialogPane().setContent(grid);
        createDefaultButtons(UI);
    }

    private void addGridRow(GridPane grid, AtomicInteger rowIndex, String title, Object value) {
        var label = label(title);

        var text = switch (value) {
            case LocalDate localDate -> DATE_FORMATTER.format(localDate);
            default -> value.toString();
        };

        var textField = textField(text, 20);
        textField.setEditable(false);
        grid.addRow(rowIndex.getAndIncrement(), label, textField);
    }
}
