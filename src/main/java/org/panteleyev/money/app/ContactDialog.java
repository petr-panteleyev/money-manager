/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Icon;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.app.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CITY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONTACT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTRY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_MOBILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_PHONE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STREET;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ZIP;
import static org.panteleyev.money.persistence.DataCache.cache;

final class ContactDialog extends BaseDialog<Contact> {
    private final ValidationSupport validation = new ValidationSupport();

    private final ComboBox<ContactType> typeBox = comboBox(ContactType.values());
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

    ContactDialog(Controller owner, URL css, Contact contact) {
        super(owner, css);

        setTitle(fxString(UI, I18N_WORD_CONTACT));

        nameField.setPrefColumnCount(20);
        IconManager.setupComboBox(iconComboBox);

        getDialogPane().setContent(
            gridPane(
                List.of(
                    gridRow(label(fxString(UI, I18N_WORD_TYPE, COLON)), typeBox, iconComboBox),
                    gridRow(label(fxString(UI, I18N_WORD_NAME, COLON)), gridCell(nameField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_PHONE, COLON)), gridCell(phoneField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_MOBILE, COLON)), gridCell(mobileField, 2, 1)),
                    gridRow(label("E-Mail:"), gridCell(emailField, 2, 1)),
                    gridRow(label("URL:"), gridCell(webField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_STREET, COLON)), gridCell(streetField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_CITY, COLON)), gridCell(cityField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_COUNTRY, COLON)), gridCell(countryField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_ZIP, COLON)), gridCell(zipField, 2, 1)),
                    gridRow(label(fxString(UI, I18N_WORD_COMMENT, COLON)), gridCell(commentEdit, 2, 1))
                        .withValignment(VPos.TOP)
                ), b -> b.withStyle(GRID_PANE)
            )
        );

        if (contact != null) {
            typeBox.getSelectionModel().select(contact.type());

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
            typeBox.getSelectionModel().select(0);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            long now = System.currentTimeMillis();

            var builder = new Contact.Builder(contact)
                .name(nameField.getText())
                .type(typeBox.getSelectionModel().getSelectedItem())
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
                builder.uuid(UUID.randomUUID())
                    .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(nameField, (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty()));

        validation.initInitialDecoration();
    }

    ComboBox<ContactType> getTypeBox() {
        return typeBox;
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
