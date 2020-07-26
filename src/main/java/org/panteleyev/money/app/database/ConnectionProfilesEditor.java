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
import static javafx.scene.control.ButtonBar.ButtonData.BIG_GAP;
import static javafx.scene.control.ButtonBar.ButtonData.LEFT;
import static javafx.scene.control.ButtonBar.ButtonData.SMALL_GAP;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.CLOSE;
import static javafx.scene.control.ButtonType.OK;
import static org.panteleyev.fx.ButtonFactory.newButtonType;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.app.Constants.COLON;
import static org.panteleyev.money.app.MainWindowController.CSS_PATH;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.Styles.GREEN_TEXT;
import static org.panteleyev.money.app.Styles.RED_TEXT;

class ConnectionProfilesEditor extends BaseDialog<Object> {
    private final ValidationSupport validation = new ValidationSupport();

    private static int counter = 0;

    private final ValidationSupport profileNameValidation = new ValidationSupport();

    private final ListView<ConnectionProfile> profileListView;
    private final TextField profileNameEdit = new TextField();
    private final TCPEditor tcpEditor;
    private final EncryptionKeyEditor encryptionKeyEditor = new EncryptionKeyEditor();
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

    ConnectionProfilesEditor(ConnectionProfileManager profileManager, boolean useEncryption) {
        super(CSS_PATH);

        Objects.requireNonNull(profileManager);

        this.profileManager = profileManager;

        profileListView = initProfileListView();

        tcpEditor = new TCPEditor(validation, this::onInitButton);

        setTitle(RB.getString("Profiles"));

        var newButtonType = newButtonType(RB, "New", LEFT);
        var deleteButtonType = newButtonType(RB, "Delete", LEFT);
        var testButtonType = newButtonType(RB, "Test", BIG_GAP);
        var saveButtonType = newButtonType(RB, "Save", SMALL_GAP);

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
                .or(encryptionKeyEditor.getValidation().invalidProperty())
                .or(profileListView.getSelectionModel().selectedItemProperty().isNull())
            );
            b.addEventFilter(ACTION, this::onSaveButton);
        });

        getButton(testButtonType).ifPresent(b -> {
            b.disableProperty().bind(validation.invalidProperty());
            b.addEventFilter(ACTION, this::onTestButton);
        });

        getButton(CLOSE).ifPresent(b -> b.setText(RB.getString("Close")));

        var root = new BorderPane();
        root.setLeft(initLeftPane());
        root.setCenter(initCenterPane(useEncryption));

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
            new Alert(CONFIRMATION, RB.getString("text.AreYouSure"), OK, CANCEL)
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
            tcpEditor.getSchema(),
            encryptionKeyEditor.getEncryptionKey()
        );
    }

    private void onInitButton(ActionEvent event) {
        event.consume();

        new Alert(CONFIRMATION, RB.getString("text.AreYouSure"), OK, CANCEL)
            .showAndWait()
            .filter(response -> response == OK)
            .ifPresent(b -> {
                var profile = buildConnectionProfile();

                var ex = profileManager.getInitDatabaseCallback().apply(profile);
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
            testStatusLabel.getStyleClass().remove(RED_TEXT);
            testStatusLabel.getStyleClass().add(GREEN_TEXT);
        });
    }

    private void testFail(String txt) {
        Platform.runLater(() -> {
            testStatusLabel.setText(txt != null ? txt : "");
            testStatusLabel.getStyleClass().remove(GREEN_TEXT);
            testStatusLabel.getStyleClass().add(RED_TEXT);
        });
    }

    private void onProfileSelected(ConnectionProfile profile) {
        tcpEditor.setProfile(profile);
        if (profile != null) {
            profileNameEdit.setText("");               // enforce validation
            profileNameEdit.setText(profile.name());
            encryptionKeyEditor.setEncryptionKey(profile.encryptionKey());
        } else {
            profileNameEdit.setText("");
            encryptionKeyEditor.setEncryptionKey("");
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

        var titled = new TitledPane(RB.getString("Profiles"), profileListView);
        titled.setCollapsible(false);
        titled.setMaxHeight(Double.MAX_VALUE);

        pane.setCenter(titled);
        return pane;
    }

    private VBox initCenterPane(boolean useEncryption) {
        var pane = new VBox();

        var hBox = new HBox(newLabel(RB, "Profile_Name", COLON), profileNameEdit);
        hBox.setAlignment(Pos.CENTER_LEFT);

        var titled = new TitledPane(RB.getString("Connection"), tcpEditor);
        titled.setCollapsible(false);


        pane.getChildren().addAll(hBox, titled);
        if (useEncryption) {
            var encryptionPane = new TitledPane(RB.getString("Encryption_Key"), encryptionKeyEditor);
            encryptionPane.setCollapsible(false);
            pane.getChildren().add(encryptionPane);
            VBox.setMargin(encryptionPane, new Insets(0.0, 0.0, 10.0, 0.0));
        }
        pane.getChildren().add(testStatusLabel);

        HBox.setHgrow(profileNameEdit, Priority.ALWAYS);
        HBox.setMargin(profileNameEdit, new Insets(0.0, 0.0, 10.0, 5.0));
        VBox.setMargin(titled, new Insets(0.0, 0.0, 10.0, 0.0));

        return pane;
    }
}
