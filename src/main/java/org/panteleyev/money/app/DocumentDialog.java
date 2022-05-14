/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Named;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DOCUMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

final class DocumentDialog extends BaseDialog<MoneyDocument> {
    private static class CompletionProvider<T extends Named> extends BaseCompletionProvider<T> {
        CompletionProvider(Set<T> set) {
            super(set, () -> settings().getAutoCompleteLength());
        }

        public String getElementString(T element) {
            return element.name();
        }
    }

    private static final Map<String, String> MIME_TYPES = Map.of(
            ".pdf", "application/pdf",
            ".xml", "application/xml"
    );

    private final ValidationSupport validation = new ValidationSupport();

    private final TextField contactEdit = new TextField();
    private final MenuButton contactMenuButton = new MenuButton();
    private final TreeSet<Contact> contactSuggestions = new TreeSet<>();

    private final TextField nameEdit = new TextField();
    private final ComboBox<DocumentType> typeComboBox =
            comboBox(DocumentType.values(), b -> b.withStringConverter(Bundles::translate));
    private final DatePicker datePicker = new DatePicker();
    private final TextField descriptionEdit = new TextField();

    private byte[] bytes = new byte[0];
    private UUID contactUuid;

    private static final ToStringConverter<Contact> CONTACT_TO_STRING = new ToStringConverter<>() {
        public String toString(Contact obj) {
            return obj.name();
        }
    };

    DocumentDialog(Controller owner, MoneyRecord documentOwner, URL css, MoneyDocument document) {
        super(owner, css);
        setTitle(fxString(UI, I18N_WORD_DOCUMENT));

        nameEdit.setEditable(false);
        descriptionEdit.setPrefColumnCount(40);
        typeComboBox.setEditable(false);
        contactMenuButton.setFocusTraversable(false);

        TextFields.bindAutoCompletion(contactEdit, new CompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);

        setupContactMenu();

        var browseButton = button("...", event -> onBrowse());
        browseButton.setDisable(document != null);
        getDialogPane().setContent(
                gridPane(List.of(
                                gridRow(label(fxString(UI, I18N_WORD_COUNTERPARTY, COLON)),
                                        contactEdit, contactMenuButton),
                                gridRow(label(fxString(UI, I18N_WORD_FILE, COLON)), nameEdit, browseButton),
                                gridRow(label(fxString(UI, I18N_WORD_TYPE, COLON)), gridCell(typeComboBox, 2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_DATE, COLON)),
                                        gridCell(datePicker, 2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_DESCRIPTION, COLON)),
                                        gridCell(descriptionEdit, 2, 1))
                        ),
                        b -> b.withStyle(GRID_PANE)
                )
        );

        if (document == null) {
            nameEdit.setText("");
            typeComboBox.getSelectionModel().select(DocumentType.OTHER);
            datePicker.setValue(LocalDate.now());
        } else {
            contactUuid = document.contactUuid();
            contactEdit.setText(cache().getContact(contactUuid).map(Contact::name).orElse(""));
            nameEdit.setText(document.fileName());
            descriptionEdit.setText(document.description());
            typeComboBox.getSelectionModel().select(document.documentType());
            datePicker.setValue(document.date());
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            long now = System.currentTimeMillis();

            var builder = new MoneyDocument.Builder(document)
                    .documentType(typeComboBox.getSelectionModel().getSelectedItem())
                    .contactUuid(contactUuid)
                    .date(datePicker.getValue())
                    .description(descriptionEdit.getText())
                    .modified(now);

            if (documentOwner != null) {
                builder.ownerUuid(documentOwner.uuid());
            }

            if (document == null) {
                builder.uuid(UUID.randomUUID())
                        .fileName(nameEdit.getText())
                        .size(bytes.length)
                        .mimeType(calculateMimeType(nameEdit.getText()))
                        .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());
        Platform.runLater(this::createValidationSupport);
    }

    byte[] getBytes() {
        return bytes;
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));

        validation.registerValidator(contactEdit, (Control control, String value) -> {
            var contact = cache().getContacts().stream()
                    .filter(c -> c.name().equals(contactEdit.getText()))
                    .findFirst();
            contactUuid = contact.map(Contact::uuid).orElse(null);
            return ValidationResult.fromErrorIf(control, null, contact.isEmpty());
        });

        validation.initInitialDecoration();
    }

    private void onBrowse() {
        var d = new FileChooser();
        var selected = d.showOpenDialog(getOwner());
        if (selected != null) {
            try {
                bytes = Files.readAllBytes(selected.toPath());
                nameEdit.setText(selected.getName());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private static String calculateMimeType(String fileName) {
        var dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            var extension = fileName.substring(dotIndex);
            return Optional.ofNullable(MIME_TYPES.get(extension)).orElse("");
        } else {
            return "";
        }
    }

    private void setupContactMenu() {
        contactMenuButton.getItems().clear();
        contactSuggestions.clear();

        cache().getContacts().stream()
                .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
                .forEach(x -> {
                    contactMenuButton.getItems().add(menuItem(x.name(), event -> onContactSelected(x)));
                    contactSuggestions.add(x);
                });

        contactMenuButton.setDisable(contactMenuButton.getItems().isEmpty());
    }

    private void onContactSelected(Contact c) {
        contactEdit.setText(c.name());
        contactUuid = c.uuid();
    }
}
