/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.exchange;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplitType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Constants.FULL_DATE_FORMAT;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Styles.BIG_SPACING;

public class ExchangeSecuritySplitsDialog extends BaseDialog<ExchangeSecuritySplit> {
    private final TableView<ExchangeSecuritySplit> table;

    private final ComboBox<ExchangeSecuritySplitType> typeComboBox = comboBox(ExchangeSecuritySplitType.values());
    private final DatePicker datePicker = new DatePicker();
    private final TextField rateEdit = new TextField();

    private final ValidationSupport validation = new ValidationSupport();

    private final UUID securityUuid;

    private final Validator<String> DECIMAL_VALIDATOR = (Control control, String value) -> {
        var invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    public ExchangeSecuritySplitsDialog(Controller owner, ExchangeSecurity security) {
        super(owner, settings().getDialogCssFileUrl());

        var splits = cache().getExchangeSecuritySplits()
                .filtered(split -> Objects.equals(split.securityUuid(), security.uuid()))
                .sorted((s1, s2) -> s2.date().compareTo(s1.date()));
        table = new TableView<>(splits);

        this.securityUuid = security.uuid();

        setTitle("Сплиты - " + security.secId() + " (" + security.name() + ")");

        setupTable();

        var content = new BorderPane(table);
        content.setBottom(setupEditorPane());
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        setResizable(true);
        getDialogPane().setPrefWidth(800);
        getDialogPane().setPrefHeight(400);

        Platform.runLater(this::createValidationSupport);
    }

    private Optional<ExchangeSecuritySplit> getSelected() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void setupTable() {
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn("Дата",
                        b -> b.withPropertyCallback(s -> s.date().format(FULL_DATE_FORMAT))
                                .withWidthBinding(w.multiply(0.33))),
                tableColumn("Тип",
                        b -> b.withPropertyCallback(ExchangeSecuritySplit::type).withWidthBinding(w.multiply(0.33))),
                tableColumn("Коэффиент",
                        b -> b.withPropertyCallback(ExchangeSecuritySplit::rate).withWidthBinding(w.multiply(0.33))))
        );

        table.getSelectionModel().selectedItemProperty().addListener((_, _, selected) -> onSplitSelected(selected));
    }

    private BorderPane setupEditorPane() {
        var pane = new BorderPane();

        datePicker.setEditable(false);

        var clearButton = button("Очистить", _ -> clear());
        clearButton.setCancelButton(true);

        var addButton = button("Добавить", this::onAddSplit);
        addButton.disableProperty().bind(validation.invalidProperty());

        var updateButton = button("Изменить", this::onEditSplit);
        updateButton.disableProperty().bind(
                table.getSelectionModel().selectedItemProperty().isNull().or(
                        validation.invalidProperty()
                )
        );

        var deleteButton = button("Удалить", this::onDeleteSplit);
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        var editRow = hBox(BIG_SPACING, datePicker, typeComboBox, rateEdit);
        VBox.setMargin(editRow, new Insets(BIG_SPACING, 0, 0, 0));

        var buttonRow = hBox(BIG_SPACING, clearButton, deleteButton, updateButton, addButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        pane.setCenter(vBox(BIG_SPACING, editRow, buttonRow));

        clear();

        return pane;
    }

    private ExchangeSecuritySplit buildSplit(UUID uuid) {
        return new ExchangeSecuritySplit.Builder()
                .uuid(uuid)
                .type(typeComboBox.getValue())
                .date(datePicker.getValue())
                .rate(new BigDecimal(rateEdit.getText()))
                .securityUuid(securityUuid)
                .build();
    }

    private void clear() {
        datePicker.setValue(LocalDate.now());
        typeComboBox.getSelectionModel().selectFirst();
        rateEdit.clear();
    }

    private void createValidationSupport() {
        validation.registerValidator(rateEdit, DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }

    private void onAddSplit(ActionEvent ignore) {
        var split = buildSplit(null);
        dao().insertExchangeSecuritySplit(split);
    }

    private void onEditSplit(ActionEvent ignore) {
        getSelected().ifPresent(split -> {
            var updated = buildSplit(split.uuid());
            dao().updateExchangeSecuritySplit(updated);
        });
    }

    private void onDeleteSplit(ActionEvent ignore) {
        getSelected().ifPresent(split -> dao().deleteExchangeSecuritySplit(split));
    }

    private void onSplitSelected(ExchangeSecuritySplit split) {
        if (split != null) {
            datePicker.setValue(split.date());
            typeComboBox.setValue(split.type());
            rateEdit.setText(split.rate().toString());
        } else {
            clear();
        }
    }
}
