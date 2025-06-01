/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import org.controlsfx.dialog.FontSelectorDialog;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;

import java.util.List;

import static javafx.collections.FXCollections.observableArrayList;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.TabFactory.tab;
import static org.panteleyev.fx.TitledPaneBuilder.titledPane;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.DOUBLE_SPACING;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.app.settings.ColorName.CREDIT;
import static org.panteleyev.money.app.settings.ColorName.DEBIT;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_CHECKED;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_MISSING;
import static org.panteleyev.money.app.settings.ColorName.STATEMENT_UNCHECKED;
import static org.panteleyev.money.app.settings.ColorName.TRANSFER;
import static org.panteleyev.money.app.settings.FontName.CONTROLS_FONT;
import static org.panteleyev.money.app.settings.FontName.DIALOG_LABEL_FONT;
import static org.panteleyev.money.app.settings.FontName.MENU_FONT;
import static org.panteleyev.money.app.settings.FontName.TABLE_CELL_FONT;

public class SettingsDialog extends BaseDialog<ButtonType> {
    private static final Validator<String> DELTA_VALIDATOR = (Control control, String value) -> {
        var invalid = false;
        try {
            Integer.parseInt(value);
            invalid = Integer.parseInt(value) <= 0;
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid);
    };

    private final ValidationSupport validation = new ValidationSupport();

    private final ChoiceBox<Integer> autoCompleteLength = new ChoiceBox<>(observableArrayList(2, 3, 4, 5));
    private final TextField accountClosingDayDeltaEdit = new TextField();

    private final TextField controlsFontField = new TextField();
    private final TextField menuFontField = new TextField();
    private final TextField cellFontField = new TextField();
    private final ColorPicker debitColorPicker = new ColorPicker();
    private final ColorPicker creditColorPicker = new ColorPicker();
    private final ColorPicker transferColorPicker = new ColorPicker();
    private final ColorPicker statementCheckedColorPicker = new ColorPicker();
    private final ColorPicker statementUncheckedColorPicker = new ColorPicker();
    private final ColorPicker statementMissingColorPicker = new ColorPicker();

    private final TextField dialogLabelFontField = new TextField();

    public SettingsDialog(Controller owner, Settings settings) {
        super(owner, settings.getDialogCssFileUrl());

        setTitle("Настройки");
        createDefaultButtons(UI, validation.invalidProperty());

        controlsFontField.setEditable(false);
        controlsFontField.setPrefColumnCount(20);
        menuFontField.setEditable(false);
        menuFontField.setPrefColumnCount(20);
        cellFontField.setEditable(false);
        cellFontField.setPrefColumnCount(20);
        dialogLabelFontField.setEditable(false);
        dialogLabelFontField.setPrefColumnCount(20);

        debitColorPicker.setValue(settings.getColor(DEBIT));
        creditColorPicker.setValue(settings.getColor(CREDIT));
        transferColorPicker.setValue(settings.getColor(TRANSFER));
        statementCheckedColorPicker.setValue(settings.getColor(STATEMENT_CHECKED));
        statementUncheckedColorPicker.setValue(settings.getColor(STATEMENT_UNCHECKED));
        statementMissingColorPicker.setValue(settings.getColor(STATEMENT_MISSING));

        setupFontField(controlsFontField, settings.getFont(CONTROLS_FONT));
        setupFontField(menuFontField, settings.getFont(MENU_FONT));
        setupFontField(cellFontField, settings.getFont(TABLE_CELL_FONT));
        setupFontField(dialogLabelFontField, settings.getFont(DIALOG_LABEL_FONT));

        var tabPane = new TabPane(
                tab("Общие", false, gridPane(
                        List.of(
                                gridRow(label("Длина префикса автодополнения:"), autoCompleteLength),
                                gridRow(label("Дней до закрытия счета:"), accountClosingDayDeltaEdit)
                        ), b -> b.withStyle(GRID_PANE))
                ),
                tab("Шрифты", false,
                        vBox(DOUBLE_SPACING,
                                titledPane("Элементы управления",
                                        gridPane(List.of(
                                                gridRow(label("Текст:"), controlsFontField,
                                                        button(ELLIPSIS, _ -> onFontSelected(controlsFontField))),
                                                gridRow(label("Меню:"), menuFontField,
                                                        button(ELLIPSIS, _ -> onFontSelected(menuFontField)))
                                        ), b -> b.withStyle(GRID_PANE))
                                ),
                                titledPane("Таблицы",
                                        gridPane(List.of(
                                                        gridRow(cellFontField,
                                                                button(ELLIPSIS, _ -> onFontSelected(cellFontField)))
                                                ), b -> b.withStyle(GRID_PANE)
                                        )
                                ),
                                titledPane("Диалоги",
                                        gridPane(List.of(
                                                        gridRow(dialogLabelFontField,
                                                                button(ELLIPSIS,
                                                                        _ -> onFontSelected(dialogLabelFontField)))
                                                ), b -> b.withStyle(GRID_PANE)
                                        )
                                )
                        )
                ),
                tab("Цвета", false,
                        vBox(DOUBLE_SPACING,
                                titledPane("Проводки",
                                        gridPane(List.of(
                                                        gridRow(label("Дебет:"), debitColorPicker),
                                                        gridRow(label("Кредит:"), creditColorPicker),
                                                        gridRow(label("Перевод:"), transferColorPicker)
                                                ), b -> b.withStyle(GRID_PANE)
                                        )
                                ),
                                titledPane("Выписки",
                                        gridPane(List.of(
                                                        gridRow(label("Подтверждено:"),
                                                                statementCheckedColorPicker),
                                                        gridRow(label("Не подтверждено:"),
                                                                statementUncheckedColorPicker),
                                                        gridRow(label("Не найдено:"),
                                                                statementMissingColorPicker)
                                                ), b -> b.withStyle(GRID_PANE)
                                        )
                                )
                        )
                )
        );
        getDialogPane().setContent(tabPane);

        autoCompleteLength.getSelectionModel().select(Integer.valueOf(settings.getAutoCompleteLength()));
        accountClosingDayDeltaEdit.setText(Integer.toString(settings.getAccountClosingDayDelta()));

        setResultConverter((ButtonType param) -> {
            if (param == ButtonType.OK) {
                settings.update(opt -> {
                    opt.setAutoCompleteLength(autoCompleteLength.getValue());
                    opt.setAccountClosingDayDelta(Integer.parseInt(accountClosingDayDeltaEdit.getText()));
                    // Fonts
                    opt.setFont(CONTROLS_FONT, (Font) controlsFontField.getUserData());
                    opt.setFont(MENU_FONT, (Font) menuFontField.getUserData());
                    opt.setFont(TABLE_CELL_FONT, (Font) cellFontField.getUserData());
                    opt.setFont(DIALOG_LABEL_FONT, (Font) dialogLabelFontField.getUserData());
                    // Colors
                    opt.setColor(DEBIT, debitColorPicker.getValue());
                    opt.setColor(CREDIT, creditColorPicker.getValue());
                    opt.setColor(TRANSFER, transferColorPicker.getValue());
                    opt.setColor(STATEMENT_CHECKED, statementCheckedColorPicker.getValue());
                    opt.setColor(STATEMENT_UNCHECKED, statementUncheckedColorPicker.getValue());
                    opt.setColor(STATEMENT_MISSING, statementMissingColorPicker.getValue());
                });
            }
            return param;
        });

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(accountClosingDayDeltaEdit, DELTA_VALIDATOR);
        validation.initInitialDecoration();
    }

    private void onFontSelected(TextField field) {
        var font = (Font) field.getUserData();
        new FontSelectorDialog(font)
                .showAndWait()
                .ifPresent(newFont -> setupFontField(field, newFont));
    }

    private static void setupFontField(TextField field, Font font) {
        field.setUserData(font);
        field.setText(String.format("%s %s, %d",
                font.getFamily(), font.getStyle(), (int) font.getSize()));
    }
}
