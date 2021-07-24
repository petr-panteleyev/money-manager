/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

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
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import java.util.List;
import static javafx.collections.FXCollections.observableArrayList;
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
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.DOUBLE_SPACING;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.app.options.ColorOption.CREDIT;
import static org.panteleyev.money.app.options.ColorOption.DEBIT;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_CHECKED;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_MISSING;
import static org.panteleyev.money.app.options.ColorOption.STATEMENT_UNCHECKED;
import static org.panteleyev.money.app.options.ColorOption.TRANSFER;
import static org.panteleyev.money.app.options.FontOption.CONTROLS_FONT;
import static org.panteleyev.money.app.options.FontOption.DIALOG_LABEL_FONT;
import static org.panteleyev.money.app.options.FontOption.MENU_FONT;
import static org.panteleyev.money.app.options.FontOption.TABLE_CELL_FONT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_AUTOCOMPLETE_PREFIX_LENGTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DAYS_BEFORE_CLOSING;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_NOT_FOUND;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COLORS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONFIRMED;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONTROLS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CREDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DEBIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DIALOGS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_FONTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_GENERAL;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_MENU;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_OPTIONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TABLES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TEXT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TRANSACTIONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TRANSFER;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_UNCONFIRMED;

public class OptionsDialog extends BaseDialog<ButtonType> {
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

    public OptionsDialog(Controller owner, Options options) {
        super(owner, options.getDialogCssFileUrl());

        setTitle(fxString(UI, I18N_WORD_OPTIONS));
        createDefaultButtons(UI, validation.invalidProperty());

        controlsFontField.setEditable(false);
        controlsFontField.setPrefColumnCount(20);
        menuFontField.setEditable(false);
        menuFontField.setPrefColumnCount(20);
        cellFontField.setEditable(false);
        cellFontField.setPrefColumnCount(20);
        dialogLabelFontField.setEditable(false);
        dialogLabelFontField.setPrefColumnCount(20);

        debitColorPicker.setValue(options.getColor(DEBIT));
        creditColorPicker.setValue(options.getColor(CREDIT));
        transferColorPicker.setValue(options.getColor(TRANSFER));
        statementCheckedColorPicker.setValue(options.getColor(STATEMENT_CHECKED));
        statementUncheckedColorPicker.setValue(options.getColor(STATEMENT_UNCHECKED));
        statementMissingColorPicker.setValue(options.getColor(STATEMENT_MISSING));

        setupFontField(controlsFontField, options.getFont(CONTROLS_FONT));
        setupFontField(menuFontField, options.getFont(MENU_FONT));
        setupFontField(cellFontField, options.getFont(TABLE_CELL_FONT));
        setupFontField(dialogLabelFontField, options.getFont(DIALOG_LABEL_FONT));

        var tabPane = new TabPane(
            newTab(UI, I18N_WORD_GENERAL, false, gridPane(
                List.of(
                    gridRow(label(fxString(UI, I18N_MISC_AUTOCOMPLETE_PREFIX_LENGTH, COLON)), autoCompleteLength),
                    gridRow(label(fxString(UI, I18N_MISC_DAYS_BEFORE_CLOSING)), accountClosingDayDeltaEdit)
                ), b -> b.withStyle(GRID_PANE))
            ),
            newTab(UI, I18N_WORD_FONTS, false,
                vBox(DOUBLE_SPACING,
                    titledPane(fxString(UI, I18N_WORD_CONTROLS),
                        gridPane(List.of(
                            gridRow(label(fxString(UI, I18N_WORD_TEXT, COLON)), controlsFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(controlsFontField))),
                            gridRow(label(fxString(UI, I18N_WORD_MENU, COLON)), menuFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(menuFontField)))
                        ), b -> b.withStyle(GRID_PANE))
                    ),
                    titledPane(fxString(UI, I18N_WORD_TABLES),
                        gridPane(List.of(
                            gridRow(cellFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(cellFontField)))
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    ),
                    titledPane(fxString(UI, I18N_WORD_DIALOGS),
                        gridPane(List.of(
                            gridRow(dialogLabelFontField,
                                button(ELLIPSIS, actionEvent -> onFontSelected(dialogLabelFontField)))
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    )
                )
            ),
            newTab(UI, I18N_WORD_COLORS, false,
                vBox(DOUBLE_SPACING,
                    titledPane(fxString(UI, I18N_WORD_TRANSACTIONS),
                        gridPane(List.of(
                            gridRow(label(fxString(UI, I18N_WORD_DEBIT, COLON)), debitColorPicker),
                            gridRow(label(fxString(UI, I18N_WORD_CREDIT, COLON)), creditColorPicker),
                            gridRow(label(fxString(UI, I18N_WORD_TRANSFER, COLON)), transferColorPicker)
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    ),
                    titledPane(fxString(UI, I18N_WORD_STATEMENTS),
                        gridPane(List.of(
                            gridRow(label(fxString(UI, I18N_WORD_CONFIRMED, COLON)), statementCheckedColorPicker),
                            gridRow(label(fxString(UI, I18N_WORD_UNCONFIRMED, COLON)), statementUncheckedColorPicker),
                            gridRow(label(fxString(UI, I18N_MISC_NOT_FOUND, COLON)), statementMissingColorPicker)
                            ), b -> b.withStyle(GRID_PANE)
                        )
                    )
                )
            )
        );
        getDialogPane().setContent(tabPane);

        autoCompleteLength.getSelectionModel().select(Integer.valueOf(options.getAutoCompleteLength()));
        accountClosingDayDeltaEdit.setText(Integer.toString(options.getAccountClosingDayDelta()));

        setResultConverter((ButtonType param) -> {
            if (param == ButtonType.OK) {
                options.update(opt -> {
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

    private static void setupFontField(TextField field, Font font) {
        field.setUserData(font);
        field.setText(String.format("%s %s, %d",
            font.getFamily(), font.getStyle(), (int) font.getSize()));
    }
}
