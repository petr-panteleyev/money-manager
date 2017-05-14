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
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.utilities.fx.BaseDialog;
import java.util.ResourceBundle;

class ContactDialog extends BaseDialog<Contact.Builder> implements Styles {
    private ResourceBundle               rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final ChoiceBox<ContactType> typeChoiceBox = new ChoiceBox<>();
    private final TextField              nameField = new TextField();
    private final TextField              phoneField = new TextField();
    private final TextField              mobileField = new TextField();
    private final TextField              emailField = new TextField();
    private final TextField              webField = new TextField();
    private final TextArea               commentEdit = new TextArea();
    private final TextField              streetField = new TextField();
    private final TextField              cityField = new TextField();
    private final TextField              countryField = new TextField();
    private final TextField              zipField = new TextField();

    private final Contact contact;

    ContactDialog(Contact contact) {
        super(MainWindowController.DIALOGS_CSS);
        this.contact = contact;

        initialize();
    }

    private void initialize() {
        setTitle(rb.getString("contact.Dialog.Title"));

        GridPane pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);

        int index = 0;
        pane.addRow(index++, new Label(rb.getString("label.Type")), typeChoiceBox);
        pane.addRow(index++, new Label(rb.getString("label.Name")), nameField);
        pane.addRow(index++, new Label(rb.getString("label.Phone")), phoneField);
        pane.addRow(index++, new Label(rb.getString("label.Mobile")), mobileField);
        pane.addRow(index++, new Label(rb.getString("label.Email")), emailField);
        pane.addRow(index++, new Label("URL:"), webField);
        pane.addRow(index++, new Label(rb.getString("label.Street")), streetField);
        pane.addRow(index++, new Label(rb.getString("label.City")), cityField);
        pane.addRow(index++, new Label(rb.getString("label.Country")), countryField);
        pane.addRow(index++, new Label(rb.getString("label.ZIP")), zipField);
        pane.addRow(index, new Label(rb.getString("label.Comment")), commentEdit);

        RowConstraints topAlignmentConstraints = new RowConstraints();
        topAlignmentConstraints.setValignment(VPos.TOP);
        for (int i = 0; i < index; i++) {
            pane.getRowConstraints().add(new RowConstraints());
        }
        pane.getRowConstraints().add(topAlignmentConstraints);

        nameField.setPrefColumnCount(20);

        typeChoiceBox.setItems(FXCollections.observableArrayList(ContactType.values()));
        typeChoiceBox.setConverter(new ReadOnlyStringConverter<ContactType>() {
            @Override
            public String toString(ContactType type) {
                return type.getName();
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
                return new Contact.Builder(this.contact)
                        .type(typeChoiceBox.getSelectionModel().getSelectedItem())
                        .name(nameField.getText())
                        .phone(phoneField.getText())
                        .mobile(mobileField.getText())
                        .email(emailField.getText())
                        .web(webField.getText())
                        .comment(commentEdit.getText())
                        .street(streetField.getText())
                        .city(cityField.getText())
                        .country(countryField.getText())
                        .zip(zipField.getText());
            } else {
                return null;
            }
        });

        getDialogPane().setContent(pane);
        createDefaultButtons(rb);
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameField, (Control control, String value)
                -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.initInitialDecoration();
    }
}
