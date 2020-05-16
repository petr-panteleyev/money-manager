/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

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
import javafx.scene.layout.RowConstraints;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.List;
import java.util.UUID;
import static org.panteleyev.fx.GridFactory.addRows;
import static org.panteleyev.fx.GridFactory.colSpan;
import static org.panteleyev.fx.GridFactory.newGridPane;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.app.Constants.COLON;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.persistence.DataCache.cache;

final class ContactDialog extends BaseDialog<Contact> {
    private final ValidationSupport validation = new ValidationSupport();

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

    ContactDialog(Controller owner, Contact contact) {
        super(owner, MainWindowController.CSS_PATH);

        setTitle(RB.getString("contact.Dialog.Title"));

        var gridPane = newGridPane(Styles.GRID_PANE);
        addRows(gridPane,
            List.of(newLabel(RB, "label.Type"), typeChoiceBox, iconComboBox),
            List.of(newLabel(RB, "label.Name"), colSpan(nameField, 2)),
            List.of(newLabel(RB, "Phone", COLON), colSpan(phoneField, 2)),
            List.of(newLabel(RB, "label.Mobile"), colSpan(mobileField, 2)),
            List.of(newLabel(RB, "Email", COLON), colSpan(emailField, 2)),
            List.of(new Label("URL:"), colSpan(webField, 2)),
            List.of(newLabel(RB, "label.Street"), colSpan(streetField, 2)),
            List.of(newLabel(RB, "label.City"), colSpan(cityField, 2)),
            List.of(newLabel(RB, "label.Country"), colSpan(countryField, 2)),
            List.of(newLabel(RB, "label.ZIP"), colSpan(zipField, 2)),
            List.of(newLabel(RB, "Comment", COLON), colSpan(commentEdit, 2))
        );

        var rowConstraints = new RowConstraints();
        var topAlignmentConstraints = new RowConstraints();
        topAlignmentConstraints.setValignment(VPos.TOP);
        for (int i = 1; i < gridPane.getRowCount(); i++) {
            gridPane.getRowConstraints().add(rowConstraints);
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
            typeChoiceBox.getSelectionModel().select(contact.type());

            nameField.setText(contact.name());
            phoneField.setText(contact.phone());
            mobileField.setText(contact.mobile());
            emailField.setText(contact.email());
            webField.setText(contact.web());
            commentEdit.setText(contact.comment());
            streetField.setText(contact.street());
            cityField.setText(contact.city());
            countryField.setText(contact.country());
            zipField.setText(contact.zip());
            iconComboBox.getSelectionModel().select(cache().getIcon(contact.iconUuid()).orElse(EMPTY_ICON));
        } else {
            typeChoiceBox.getSelectionModel().select(0);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                long now = System.currentTimeMillis();

                var builder = new Contact.Builder(contact)
                    .name(nameField.getText())
                    .type(typeChoiceBox.getSelectionModel().getSelectedItem())
                    .phone(phoneField.getText())
                    .mobile(mobileField.getText())
                    .email(emailField.getText())
                    .web(webField.getText())
                    .comment(commentEdit.getText())
                    .street(streetField.getText())
                    .city(cityField.getText())
                    .country(countryField.getText())
                    .zip(zipField.getText())
                    .iconUuid(iconComboBox.getSelectionModel().getSelectedItem().uuid())
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

        createDefaultButtons(RB, validation.invalidProperty());
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
