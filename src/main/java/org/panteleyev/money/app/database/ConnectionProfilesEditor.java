/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.database;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.ReadOnlyStringConverter;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import static javafx.event.ActionEvent.ACTION;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonBar.ButtonData.BIG_GAP;
import static javafx.scene.control.ButtonBar.ButtonData.LEFT;
import static javafx.scene.control.ButtonBar.ButtonData.SMALL_GAP;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.CLOSE;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.control.ButtonType.YES;
import static org.panteleyev.fx.ButtonFactory.buttonType;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.bundles.Internationalization.I18M_MISC_SCHEMA_RESET_HEADER;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ARE_YOU_SURE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_PROFILE_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_SCHEMA_RESET;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_SCHEMA_RESET_TEXT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONNECTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DELETE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_PROFILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SAVE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TEST;

class ConnectionProfilesEditor extends BaseDialog<Object> {
    private final ValidationSupport validation = new ValidationSupport();

    private static int counter = 0;

    private final ValidationSupport profileNameValidation = new ValidationSupport();

    private final ListView<ConnectionProfile> profileListView;
    private final TextField profileNameEdit = new TextField();
    private final TCPEditor tcpEditor;
    private final Label testStatusLabel = new Label();

    private final ConnectionProfileManager profileManager;

    private static final Validator<String> INTEGER_VALIDATOR = (Control control, String text) -> {
        var invalid = false;
        try {
            Integer.parseInt(text);
        } catch (Exception ex) {
            invalid = true;
        }
        return ValidationResult.fromErrorIf(control, null, invalid);
    };

    ConnectionProfilesEditor(ConnectionProfileManager profileManager) {
        super(options().getDialogCssFileUrl());

        Objects.requireNonNull(profileManager);

        this.profileManager = profileManager;

        profileListView = initProfileListView();

        tcpEditor = new TCPEditor(validation, this::onResetButton);

        setTitle(fxString(UI, I18N_WORD_PROFILES));

        var newButtonType = buttonType(fxString(UI, I18N_WORD_ADD), LEFT);
        var deleteButtonType = buttonType(fxString(UI, I18N_WORD_DELETE), LEFT);
        var testButtonType = buttonType(fxString(UI, I18N_WORD_TEST), BIG_GAP);
        var saveButtonType = buttonType(fxString(UI, I18N_WORD_SAVE), SMALL_GAP);

        getDialogPane().getButtonTypes().addAll(
            newButtonType, deleteButtonType, testButtonType, saveButtonType, CLOSE
        );

        getButton(newButtonType).ifPresent(b -> b.addEventFilter(ACTION, this::onNewButton));

        getButton(deleteButtonType).ifPresent(b -> {
            b.disableProperty().bind(profileListView.getSelectionModel().selectedItemProperty().isNull());
            b.addEventFilter(ACTION, this::onDeleteButton);
        });

        getButton(saveButtonType).ifPresent(b -> {
            b.disableProperty().bind(profileNameValidation.invalidProperty()
                .or(profileListView.getSelectionModel().selectedItemProperty().isNull())
            );
            b.addEventFilter(ACTION, this::onSaveButton);
        });

        getButton(testButtonType).ifPresent(b -> {
            b.disableProperty().bind(validation.invalidProperty());
            b.addEventFilter(ACTION, this::onTestButton);
        });

        getButton(CLOSE).ifPresent(b -> b.setText(fxString(UI, I18N_WORD_CLOSE)));

        var root = new BorderPane();
        root.setLeft(initLeftPane());
        root.setCenter(initCenterPane());

        BorderPane.setMargin(root.getCenter(), new Insets(0.0, 0.0, 5.0, 10.0));

        getDialogPane().setContent(root);

        Platform.runLater(this::createValidationSupport);
    }

    private Optional<ConnectionProfile> getSelectedProfile() {
        return Optional.ofNullable(profileListView.getSelectionModel().getSelectedItem());
    }

    private ListView<ConnectionProfile> initProfileListView() {
        var listView = new ListView<ConnectionProfile>();

        listView.setCellFactory(param -> {
            TextFieldListCell<ConnectionProfile> cell = new TextFieldListCell<>();
            cell.setConverter(new ReadOnlyStringConverter<>() {
                @Override
                public String toString(ConnectionProfile profile) {
                    return profile.name() != null ? profile.name() : "";
                }
            });
            return cell;
        });

        listView.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> onProfileSelected(newValue));

        listView.addEventFilter(MouseEvent.ANY, event -> {
            if (profileNameValidation.isInvalid()) {
                event.consume();
            }
        });

        listView.setItems(FXCollections.observableArrayList(profileManager.getAll()));
        return listView;
    }

    private void onDeleteButton(ActionEvent event) {
        event.consume();
        getSelectedProfile().ifPresent(selected ->
            new Alert(CONFIRMATION, fxString(UI, I18N_MISC_ARE_YOU_SURE), OK, CANCEL)
                .showAndWait()
                .filter(response -> response == OK)
                .ifPresent(b -> {
                    profileManager.deleteProfile(selected);
                    profileManager.saveProfiles();
                    profileListView.getItems().remove(selected);
                }));
    }

    private void onSaveButton(ActionEvent event) {
        event.consume();

        int index = profileListView.getSelectionModel().getSelectedIndex();

        var prof = buildConnectionProfile();

        profileListView.getItems().set(index, prof);
        profileListView.getSelectionModel().select(prof);

        profileManager.setProfiles(profileListView.getItems());
        profileManager.saveProfiles();
    }

    private ConnectionProfile buildConnectionProfile() {
        return new ConnectionProfile(
            profileNameEdit.getText(),
            tcpEditor.getDataBaseHost(),
            tcpEditor.getDataBasePort(),
            tcpEditor.getDataBaseUser(),
            tcpEditor.getDataBasePassword(),
            tcpEditor.getSchema()
        );
    }

    private void onResetButton(ActionEvent event) {
        event.consume();

        var alert = new Alert(WARNING, UI.getString(I18N_MISC_SCHEMA_RESET_TEXT), YES, NO);
        alert.setHeaderText(fxString(UI, I18M_MISC_SCHEMA_RESET_HEADER));
        alert.setTitle(fxString(UI, I18N_MISC_SCHEMA_RESET));

        alert.showAndWait()
            .filter(response -> response == YES)
            .ifPresent(b -> {
                var profile = buildConnectionProfile();

                var ex = profileManager.getResetDatabaseCallback().apply(profile);
                if (ex != null) {
                    testFail(ex.getMessage());
                } else {
                    testSuccess();
                }
            });
    }

    private void onTestButton(ActionEvent event) {
        event.consume();

        var profile = buildConnectionProfile();

        var TEST_QUERY = "SHOW TABLES FROM " + profile.schema();

        var ds = profileManager.getBuildDataSourceCallback().apply(profile);

        try (var conn = ds.getConnection(); var st = conn.createStatement()) {
            st.execute(TEST_QUERY);
            testSuccess();
        } catch (SQLException ex) {
            testFail(ex.getMessage());
        }
    }

    private void testSuccess() {
        Platform.runLater(() -> {
            testStatusLabel.setText("Success");
            testStatusLabel.textFillProperty().set(Color.GREEN);
        });
    }

    private void testFail(String txt) {
        Platform.runLater(() -> {
            testStatusLabel.setText(txt != null ? txt : "");
            testStatusLabel.textFillProperty().set(Color.RED);
        });
    }

    private void onProfileSelected(ConnectionProfile profile) {
        tcpEditor.setProfile(profile);
        if (profile != null) {
            profileNameEdit.setText("");               // enforce validation
            profileNameEdit.setText(profile.name());
        } else {
            profileNameEdit.setText("");
        }
    }

    private void onNewButton(ActionEvent event) {
        event.consume();

        var profile = new ConnectionProfile("New Profile" + (++counter), "money");
        profileListView.getItems().add(profile);
        profileListView.getSelectionModel().select(profile);
    }

    private void createValidationSupport() {
        validation.registerValidator(tcpEditor.getSchemaEdit(), (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBaseHostEdit(), (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBaseUserEdit(), (Control control, String value) ->
            ValidationResult.fromErrorIf(control, null, value.isEmpty())
        );
        validation.registerValidator(tcpEditor.getDataBasePortEdit(), INTEGER_VALIDATOR);
        validation.initInitialDecoration();

        profileNameValidation.registerValidator(profileNameEdit, (Control control, String value) -> {
            var selected = getSelectedProfile().orElse(null);

            return ValidationResult.fromErrorIf(control, null,
                profileListView.getItems().stream().anyMatch(p -> p != selected && p.name().equals(value)));
        });

        profileNameValidation.initInitialDecoration();
    }

    private BorderPane initLeftPane() {
        var pane = new BorderPane();

        var titled = new TitledPane(fxString(UI, I18N_WORD_PROFILES), profileListView);
        titled.setCollapsible(false);
        titled.setMaxHeight(Double.MAX_VALUE);

        pane.setCenter(titled);
        return pane;
    }

    private VBox initCenterPane() {
        var pane = new VBox();

        var hBox = new HBox(label(fxString(UI, I18N_MISC_PROFILE_NAME, COLON)), profileNameEdit);
        hBox.setAlignment(Pos.CENTER_LEFT);

        var titled = new TitledPane(UI.getString(I18N_WORD_CONNECTION), tcpEditor);
        titled.setCollapsible(false);


        pane.getChildren().addAll(hBox, titled);
        pane.getChildren().add(testStatusLabel);

        HBox.setHgrow(profileNameEdit, Priority.ALWAYS);
        HBox.setMargin(profileNameEdit, new Insets(0.0, 0.0, 10.0, 5.0));
        VBox.setMargin(titled, new Insets(0.0, 0.0, 10.0, 0.0));

        return pane;
    }
}
