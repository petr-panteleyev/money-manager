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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;

public class ContactDialog extends BaseDialog<Contact.Builder> implements Initializable {
    private static final String FXML = "/org/panteleyev/money/ContactDialog.fxml";

    @FXML private ChoiceBox<ContactType>    typeChoiceBox;
    @FXML private TextField                 nameField;
    @FXML private TextField                 phoneField;
    @FXML private TextField                 mobileField;
    @FXML private TextField                 emailField;
    @FXML private TextField                 webField;
    @FXML private TextArea                  commentEdit;
    @FXML private TextField                 streetField;
    @FXML private TextField                 cityField;
    @FXML private TextField                 countryField;
    @FXML private TextField                 zipField;

    private final Contact contact;

    public ContactDialog(Contact contact) {
        super(FXML, MainWindowController.UI_BUNDLE_PATH);
        this.contact = contact;
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        setTitle(rb.getString("contact.Dialog.Title"));

        typeChoiceBox.setItems(FXCollections.observableArrayList(MoneyDAO.getInstance().getContactTypes()));
        typeChoiceBox.setConverter(new ReadOnlyStringConverter<ContactType>() {
            @Override
            public String toString(ContactType type) {
                return type.getName();
            }
        });

        if (contact != null) {
            ContactType type = MoneyDAO.getInstance().getContactType(contact.getTypeId()).get();
            typeChoiceBox.getSelectionModel().select(type);

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
                        .typeId(typeChoiceBox.getSelectionModel().getSelectedItem().getId())
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

        createDefaultButtons();
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameField, (Control control, String value)
                -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.initInitialDecoration();
    }
}
