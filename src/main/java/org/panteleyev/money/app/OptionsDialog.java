/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import java.util.List;
import static org.panteleyev.fx.GridFactory.addRows;
import static org.panteleyev.fx.GridFactory.newGridPane;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.app.MainWindowController.RB;

class OptionsDialog extends BaseDialog<ButtonType> {
    private final ValidationSupport validation = new ValidationSupport();

    private final ChoiceBox<Integer> autoCompleteLength = new ChoiceBox<>(FXCollections.observableArrayList(2, 3, 4, 5));
    private final TextField accountClosingDayDeltaEdit = new TextField();
    private final PasswordField ymToken = new PasswordField();

    OptionsDialog(Controller owner) {
        super(owner, MainWindowController.CSS_PATH);

        setTitle(RB.getString("options.Dialog.Title"));
        createDefaultButtons(RB, validation.invalidProperty());

        var pane = newGridPane(Styles.GRID_PANE);
        addRows(pane,
            List.of(newLabel(RB, "options.Dialog.Prefix.Length"), autoCompleteLength),
            List.of(newLabel(RB, "options.Dialog.closing.day.delta"), accountClosingDayDeltaEdit),
            List.of(newLabel(RB, "label.YandexMoneyToken"), ymToken)
        );
        getDialogPane().setContent(pane);

        autoCompleteLength.getSelectionModel().select(Integer.valueOf(Options.getAutoCompleteLength()));
        accountClosingDayDeltaEdit.setText(Integer.toString(Options.getAccountClosingDayDelta()));
        ymToken.setText(Options.getYandexMoneyToken());
        ymToken.setPrefColumnCount(10);

        setResultConverter((ButtonType param) -> {
            if (param == ButtonType.OK) {
                Options.setAutoCompleteLength(autoCompleteLength.getValue());
                Options.setAccountClosingDayDelta(Integer.parseInt(accountClosingDayDeltaEdit.getText()));
                Options.setYandexMoneyToken(ymToken.getText());
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
}
