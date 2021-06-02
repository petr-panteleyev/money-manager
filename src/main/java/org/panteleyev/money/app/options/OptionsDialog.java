/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import org.controlsfx.dialog.FontSelectorDialog;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import java.util.List;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxFactory.newTab;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.TitledPaneBuilder.titledPane;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.DOUBLE_SPACING;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;
import static org.panteleyev.money.app.options.ColorOption.CREDIT;
import static org.panteleyev.money.app.options.ColorOption.DEBIT;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_CHECKED;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_MISSING;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_UNCHECKED;
import static org.panteleyev.money.app.options.ColorOption.TRANSFER;
import static org.panteleyev.money.app.options.Options.options;

public class OptionsDialog extends BaseDialog<ButtonType> {
    private final ValidationSupport validation = new ValidationSupport();

    private final ChoiceBox<Integer> autoCompleteLength = new ChoiceBox<>(FXCollections.observableArrayList(2, 3, 4, 5));
    private final TextField accountClosingDayDeltaEdit = new TextField();
    private final PasswordField ymToken = new PasswordField();

    // Font text fields
    private final TextField controlsFontField = new TextField();
    private final TextField menuFontField = new TextField();
    private final TextField cellFontField = new TextField();
    private final ColorPicker debitColorPicker = new ColorPicker(DEBIT.getColor());
    private final ColorPicker creditColorPicker = new ColorPicker(CREDIT.getColor());
    private final ColorPicker transferColorPicker = new ColorPicker(TRANSFER.getColor());
    // Statement background colors
    private final ColorPicker statementCheckedColorPicker = new ColorPicker(STATEMENT_CHECKED.getColor());
    private final ColorPicker statementUncheckedColorPicker = new ColorPicker(STATEMENT_UNCHECKED.getColor());
    private final ColorPicker statementMissingColorPicker = new ColorPicker(STATEMENT_MISSING.getColor());

    private final TextField dialogLabelFontField = new TextField();

    public OptionsDialog(Controller owner) {
        super(owner, options().getDialogCssFileUrl());

        setTitle(RB.getString("options.Dialog.Title"));
        createDefaultButtons(RB, validation.invalidProperty());

        controlsFontField.setEditable(false);
        controlsFontField.setPrefColumnCount(20);
        menuFontField.setEditable(false);
        menuFontField.setPrefColumnCount(20);
        cellFontField.setEditable(false);
        cellFontField.setPrefColumnCount(20);
        dialogLabelFontField.setEditable(false);
        dialogLabelFontField.setPrefColumnCount(20);

        loadFont(FontOption.CONTROLS_FONT, controlsFontField);
        loadFont(FontOption.MENU_FONT, menuFontField);
        loadFont(FontOption.TABLE_CELL_FONT, cellFontField);
        loadFont(FontOption.DIALOG_LABEL_FONT, dialogLabelFontField);

        var tabPane = new TabPane(
            newTab(RB, "General", false, gridPane(
                List.of(
                    gridRow(label(fxString(RB, "options.Dialog.Prefix.Length")), autoCompleteLength),
                    gridRow(label(fxString(RB, "options.Dialog.closing.day.delta")), accountClosingDayDeltaEdit),
                    gridRow(label(fxString(RB, "label.YandexMoneyToken")), ymToken)
                ), b -> b.withStyle(GRID_PANE))
            ),
            newTab(RB, "Fonts", false,
                vBox(10,
                    titledPane(fxString(RB, "Controls"),
                        gridPane(List.of(
                            gridRow(label(fxString(RB, "Text", COLON)), controlsFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(controlsFontField))),
                            gridRow(label(fxString(RB, "Menu", COLON)), menuFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(menuFontField)))
                        ), b -> b.withStyle(GRID_PANE))
                    ),
                    titledPane(fxString(RB, "Tables"),
                        vBox(BIG_SPACING,
                            hBox(SMALL_SPACING, cellFontField, button(ELLIPSIS,
                                actionEvent -> onFontSelected(cellFontField)))
                        )
                    ),
                    titledPane(fxString(RB, "Dialogs"),
                        gridPane(List.of(
                            gridRow(dialogLabelFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(dialogLabelFontField)))
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    )
                )
            ),
            newTab(RB, "Colors", false,
                vBox(DOUBLE_SPACING,
                    titledPane(fxString(RB, "Transactions"),
                        gridPane(List.of(
                            gridRow(label(fxString(RB, "Debit", COLON)), debitColorPicker),
                            gridRow(label(fxString(RB, "Credit", COLON)), creditColorPicker),
                            gridRow(label(fxString(RB, "Transfer", COLON)), transferColorPicker)
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    ),
                    titledPane(fxString(RB, "Statements"),
                        gridPane(List.of(
                            gridRow(label(fxString(RB, "Confirmed", COLON)), statementCheckedColorPicker),
                            gridRow(label(fxString(RB, "Unconfirmed", COLON)), statementUncheckedColorPicker),
                            gridRow(label(fxString(RB, "Not_found", COLON)), statementMissingColorPicker)
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    )
                )
            )
        );
        getDialogPane().setContent(tabPane);

        autoCompleteLength.getSelectionModel().select(Integer.valueOf(Options.getAutoCompleteLength()));
        accountClosingDayDeltaEdit.setText(Integer.toString(Options.getAccountClosingDayDelta()));
        ymToken.setText(Options.getYandexMoneyToken());
        ymToken.setPrefColumnCount(10);

        setResultConverter((ButtonType param) -> {
            if (param == ButtonType.OK) {
                Options.setAutoCompleteLength(autoCompleteLength.getValue());
                Options.setAccountClosingDayDelta(Integer.parseInt(accountClosingDayDeltaEdit.getText()));
                Options.setYandexMoneyToken(ymToken.getText());
                // Fonts
                Options.setFont(FontOption.CONTROLS_FONT, (Font) controlsFontField.getUserData());
                Options.setFont(FontOption.MENU_FONT, (Font) menuFontField.getUserData());
                Options.setFont(FontOption.TABLE_CELL_FONT, (Font) cellFontField.getUserData());
                Options.setFont(FontOption.DIALOG_LABEL_FONT, (Font) dialogLabelFontField.getUserData());
                // Colors
                Options.setColor(DEBIT, debitColorPicker.getValue());
                Options.setColor(CREDIT, creditColorPicker.getValue());
                Options.setColor(TRANSFER, transferColorPicker.getValue());
                Options.setColor(STATEMENT_CHECKED, statementCheckedColorPicker.getValue());
                Options.setColor(STATEMENT_UNCHECKED, statementUncheckedColorPicker.getValue());
                Options.setColor(STATEMENT_MISSING, statementMissingColorPicker.getValue());

                options().generateCssFiles();
                options().reloadCssFile();
            }
            return param;
        });

        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(accountClosingDayDeltaEdit, (Control control, String value) -> {
            var invalid = false;
            try {
                Integer.parseInt(value);
                invalid = Integer.parseInt(value) <= 0;
            } catch (NumberFormatException ex) {
                invalid = true;
            }

            return ValidationResult.fromErrorIf(control, null, invalid);
        });

        validation.initInitialDecoration();
    }

    private void onFontSelected(TextField field) {
        var font = (Font) field.getUserData();
        new FontSelectorDialog(font)
            .showAndWait()
            .ifPresent(newFont -> setupFontField(field, newFont));
    }

    private void loadFont(FontOption option, TextField field) {
        setupFontField(field, option.getFont());
    }

    private void setupFontField(TextField field, Font font) {
        field.setUserData(font);
        field.setText(String.format("%s %s, %d",
            font.getFamily(), font.getStyle(), (int) font.getSize()));
    }
}
