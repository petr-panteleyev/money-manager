/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
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
import org.panteleyev.money.app.util.NamedCompletionProvider;
import org.panteleyev.money.app.util.StringCompletionProvider;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.MoneyRecord;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.choicebox.ChoiceBoxBuilder.choiceBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;

record DocumentWithBytes(MoneyDocument document, byte[] bytes) {
}

final class DocumentDialog extends BaseDialog<List<DocumentWithBytes>> {
    private record FileInfo(String name, String mimeType, byte[] bytes) {
    }

    private final ValidationSupport validation = new ValidationSupport();

    private final TextField contactEdit = new TextField();
    private final MenuButton contactMenuButton = new MenuButton();
    private final Set<Contact> contactSuggestions = new TreeSet<>();

    private final TextField nameEdit = new TextField();
    private final ChoiceBox<DocumentType> typeChoiceBox = choiceBox(DocumentType.values(),
            b -> b.withStringConverter(Bundles::translate));
    private final DatePicker datePicker = new DatePicker();
    private final TextField descriptionEdit = new TextField();

    private UUID contactUuid;
    private final List<FileInfo> fileInfos = new ArrayList<>();

    private static final ToStringConverter<Contact> CONTACT_TO_STRING = new ToStringConverter<>() {
        public String toString(Contact obj) {
            return obj.name();
        }
    };

    DocumentDialog(Controller owner, MoneyRecord documentOwner, URL css, MoneyDocument document) {
        this(owner, documentOwner, css, document, List.of());
    }

    DocumentDialog(Controller owner, MoneyRecord documentOwner, URL css, MoneyDocument document, List<File> files) {
        super(owner, css);
        setTitle("Документ");

        nameEdit.setEditable(document != null);
        nameEdit.setPrefColumnCount(40);
        descriptionEdit.setPrefColumnCount(40);
        contactMenuButton.setFocusTraversable(false);

        TextFields.bindAutoCompletion(contactEdit, new NamedCompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);

        var descriptionSuggestions = new TreeSet<>(cache().getUniqueDocumentDescriptions());
        TextFields.bindAutoCompletion(descriptionEdit, new StringCompletionProvider(descriptionSuggestions));

        setupContactMenu();

        var browseButton = button("...", event -> onBrowse());
        browseButton.setDisable(document != null || !files.isEmpty());
        getDialogPane().setContent(
                gridPane(List.of(
                                gridRow(label("Контрагент:"),
                                        contactEdit, contactMenuButton),
                                gridRow(label("Файл:"), nameEdit, browseButton),
                                gridRow(label("Тип:"), gridCell(typeChoiceBox, 2, 1)),
                                gridRow(label("Дата:"),
                                        gridCell(datePicker, 2, 1)),
                                gridRow(label("Описание:"),
                                        gridCell(descriptionEdit, 2, 1))
                        ),
                        b -> b.withStyle(GRID_PANE)
                )
        );

        if (document == null) {
            nameEdit.setText("");
            typeChoiceBox.getSelectionModel().select(DocumentType.OTHER);
            datePicker.setValue(LocalDate.now());
            readFiles(files);
        } else {
            contactUuid = document.contactUuid();
            contactEdit.setText(cache().getContact(contactUuid).map(Contact::name).orElse(""));
            nameEdit.setText(document.fileName());
            descriptionEdit.setText(document.description());
            typeChoiceBox.getSelectionModel().select(document.documentType());
            datePicker.setValue(document.date());
            fileInfos.add(new FileInfo(document.fileName(), document.mimeType(), new byte[0]));
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            long now = System.currentTimeMillis();

            var result = new ArrayList<DocumentWithBytes>(fileInfos.size());
            for (var fileInfo : fileInfos) {
                var builder = new MoneyDocument.Builder(document)
                        .documentType(typeChoiceBox.getSelectionModel().getSelectedItem())
                        .contactUuid(contactUuid)
                        .date(datePicker.getValue())
                        .description(descriptionEdit.getText())
                        .modified(now);

                if (documentOwner != null) {
                    builder.ownerUuid(documentOwner.uuid());
                }

                if (document == null) {
                    builder.uuid(UUID.randomUUID())
                            .fileName(fileInfo.name())
                            .size(fileInfo.bytes().length)
                            .mimeType(fileInfo.mimeType() == null ? "" : fileInfo.mimeType())
                            .created(now);
                } else {
                    builder.fileName(nameEdit.getText());
                }

                result.add(new DocumentWithBytes(builder.build(), fileInfo.bytes()));
            }

            return result;
        });

        createDefaultButtons(UI, validation.invalidProperty());
        Platform.runLater(this::createValidationSupport);
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
        var selected = new FileChooser().showOpenMultipleDialog(getOwner());
        if (selected != null && !selected.isEmpty()) {
            readFiles(selected);
        }
    }

    private void readFiles(List<File> files) {
        fileInfos.clear();
        for (var file : files) {
            try {
                var path = file.toPath();
                fileInfos.add(new FileInfo(
                        file.getName(), Files.probeContentType(path), Files.readAllBytes(path)
                ));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        nameEdit.setText(
                fileInfos.stream()
                        .map(f -> f.name)
                        .collect(Collectors.joining(","))
        );
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
