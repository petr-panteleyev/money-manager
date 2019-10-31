/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.commons.fx.BaseDialog;
import org.panteleyev.money.icons.IconManager;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.UUID;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.persistence.DataCache.cache;

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
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();

    ContactDialog(Contact contact) {
        super(MainWindowController.CSS_PATH);

        setTitle(RB.getString("contact.Dialog.Title"));

        var gridPane = new GridPane();

        gridPane.getStyleClass().add(Styles.GRID_PANE);

        int index = 0;
        gridPane.addRow(index++, new Label(RB.getString("label.Type")), typeChoiceBox, iconComboBox);
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

        GridPane.setColumnSpan(nameField, 2);
        GridPane.setColumnSpan(phoneField, 2);
        GridPane.setColumnSpan(mobileField, 2);
        GridPane.setColumnSpan(emailField, 2);
        GridPane.setColumnSpan(webField, 2);
        GridPane.setColumnSpan(streetField, 2);
        GridPane.setColumnSpan(cityField, 2);
        GridPane.setColumnSpan(countryField, 2);
        GridPane.setColumnSpan(zipField, 2);
        GridPane.setColumnSpan(commentEdit, 2);

        var topAlignmentConstraints = new RowConstraints();
        topAlignmentConstraints.setValignment(VPos.TOP);
        for (int i = 0; i < index; i++) {
            gridPane.getRowConstraints().add(new RowConstraints());
        }
        gridPane.getRowConstraints().add(topAlignmentConstraints);

        getDialogPane().setContent(gridPane);

        nameField.setPrefColumnCount(20);

        IconManager.setupComboBox(iconComboBox);

        typeChoiceBox.setItems(FXCollections.observableArrayList(ContactType.values()));
        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
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
            iconComboBox.getSelectionModel().select(cache().getIcon(contact.getIconUuid()).orElse(EMPTY_ICON));
        } else {
            typeChoiceBox.getSelectionModel().select(0);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                long now = System.currentTimeMillis();

                var builder = new Contact.Builder(contact)
                    .name(nameField.getText())
                    .typeId(typeChoiceBox.getSelectionModel().getSelectedItem().getId())
                    .phone(phoneField.getText())
                    .mobile(mobileField.getText())
                    .email(emailField.getText())
                    .web(webField.getText())
                    .comment(commentEdit.getText())
                    .street(streetField.getText())
                    .city(cityField.getText())
                    .country(countryField.getText())
                    .zip(zipField.getText())
                    .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().getUuid())
                    .modified(now);

                if (contact == null) {
                    builder.guid(UUID.randomUUID())
                        .created(now);
                }

                return builder.build();
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

    ChoiceBox<ContactType> getTypeChoiceBox() {
        return typeChoiceBox;
    }

    TextField getNameField() {
        return nameField;
    }

    TextField getPhoneField() {
        return phoneField;
    }

    TextField getMobileField() {
        return mobileField;
    }

    TextField getEmailField() {
        return emailField;
    }

    TextField getWebField() {
        return webField;
    }

    TextArea getCommentEdit() {
        return commentEdit;
    }

    TextField getStreetField() {
        return streetField;
    }

    TextField getCityField() {
        return cityField;
    }

    TextField getCountryField() {
        return countryField;
    }

    TextField getZipField() {
        return zipField;
    }
}
