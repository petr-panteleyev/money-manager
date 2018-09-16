/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.ContactType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.utilities.fx.BaseDialog;
import java.util.UUID;
import static org.panteleyev.money.MainWindowController.RB;

final class ContactDialog extends BaseDialog<Contact> {
    private final ChoiceBox<ContactType> typeChoiceBox = new ChoiceBox<>();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField mobileField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField webField = new TextField();
    private final TextArea commentEdit = new TextArea();
    private final TextField streetField = new TextField();
    private final TextField cityField = new TextField();
    private final TextField countryField = new TextField();
    private final TextField zipField = new TextField();

    ContactDialog(Contact contact) {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("contact.Dialog.Title"));

        GridPane gridPane = new GridPane();

        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, new Label(RB.getString("label.Type")), typeChoiceBox);
        gridPane.addRow(index++, new Label(RB.getString("label.Name")), nameField);
        gridPane.addRow(index++, new Label(RB.getString("label.Phone")), phoneField);
        gridPane.addRow(index++, new Label(RB.getString("label.Mobile")), mobileField);
        gridPane.addRow(index++, new Label(RB.getString("label.Email")), emailField);
        gridPane.addRow(index++, new Label("URL:"), webField);
        gridPane.addRow(index++, new Label(RB.getString("label.Street")), streetField);
        gridPane.addRow(index++, new Label(RB.getString("label.City")), cityField);
        gridPane.addRow(index++, new Label(RB.getString("label.Country")), countryField);
        gridPane.addRow(index++, new Label(RB.getString("label.ZIP")), zipField);
        gridPane.addRow(index, new Label(RB.getString("label.Comment")), commentEdit);

        RowConstraints topAlignmentConstraints = new RowConstraints();
        topAlignmentConstraints.setValignment(VPos.TOP);
        for (int i = 0; i < index; i++) {
            gridPane.getRowConstraints().add(new RowConstraints());
        }
        gridPane.getRowConstraints().add(topAlignmentConstraints);

        getDialogPane().setContent(gridPane);

        nameField.setPrefColumnCount(20);

        typeChoiceBox.setItems(FXCollections.observableArrayList(ContactType.values()));
        typeChoiceBox.setConverter(new ReadOnlyStringConverter<ContactType>() {
            public String toString(ContactType type) {
                return type.getTypeName();
            }
        });

        if (contact != null) {
            typeChoiceBox.getSelectionModel().select(contact.getType());

            nameField.setText(contact.getName());
            phoneField.setText(contact.getPhone());
            mobileField.setText(contact.getMobile());
            emailField.setText(contact.getEmail());
            webField.setText(contact.getWeb());
            commentEdit.setText(contact.getComment());
            streetField.setText(contact.getStreet());
            cityField.setText(contact.getCity());
            countryField.setText(contact.getCountry());
            zipField.setText(contact.getZip());
        } else {
            typeChoiceBox.getSelectionModel().select(0);
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                return new Contact(contact != null ? contact.getId() :0,
                        nameField.getText(),
                        typeChoiceBox.getSelectionModel().getSelectedItem().getId(),
                        phoneField.getText(),
                        mobileField.getText(),
                        emailField.getText(),
                        webField.getText(),
                        commentEdit.getText(),
                        streetField.getText(),
                        cityField.getText(),
                        countryField.getText(),
                        zipField.getText(),
                        contact != null? contact.getGuid() :UUID.randomUUID().toString(),
                        System.currentTimeMillis()
                );
            } else {
                return null;
            }
        });

        createDefaultButtons(RB);
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameField, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));

        validation.initInitialDecoration();
    }
}
