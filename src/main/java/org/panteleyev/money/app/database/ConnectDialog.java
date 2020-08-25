/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.database;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.ToStringConverter;
import java.util.Objects;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.Constants.COLON;
import static org.panteleyev.money.app.MainWindowController.CSS_PATH;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.Styles.GRID_PANE;

public class ConnectDialog extends BaseDialog<ConnectionProfile> {
    private final ComboBox<ConnectionProfile> profileComboBox;
    private final CheckBox defaultCheck;
    private final CheckBox autoConnectCheck;

    private final ConnectionProfileManager profileManager;

    public ConnectDialog(ConnectionProfileManager profileManager) {
        super(CSS_PATH);
        Objects.requireNonNull(profileManager);

        this.profileManager = profileManager;

        profileComboBox = initProfileComboBox();
        defaultCheck = initDefaultCheck();
        autoConnectCheck = initAutoConnectCheck();

        setTitle(RB.getString("Connection"));

        var pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);
        pane.addRow(0, label(fxString(RB, "Profile", COLON)), profileComboBox);
        pane.addRow(1, defaultCheck);
        pane.addRow(2, autoConnectCheck);
        getDialogPane().setContent(pane);

        GridPane.setColumnSpan(defaultCheck, 2);
        GridPane.setColumnSpan(autoConnectCheck, 2);

        profileComboBox.setMaxWidth(Double.MAX_VALUE);

        createDefaultButtons(RB);

        setResultConverter(b -> {
            if (b == ButtonType.OK) {
                ConnectionProfile selected = profileComboBox.getSelectionModel().getSelectedItem();
                if (defaultCheck.isSelected()) {
                    profileManager.setDefaultProfile(selected);
                }
                profileManager.setAutoConnect(autoConnectCheck.isSelected());
                profileManager.saveProfiles();
                return selected;
            } else {
                return null;
            }
        });
    }

    private ComboBox<ConnectionProfile> initProfileComboBox() {
        var cb = new ComboBox<ConnectionProfile>();

        cb.setItems(FXCollections.observableArrayList(profileManager.getAll()));

        var defaultProfile = profileManager.getDefaultProfile();
        defaultProfile.ifPresentOrElse(p -> cb.getSelectionModel().select(p), () -> {
            if (profileManager.size() > 0) {
                cb.getSelectionModel().select(0);
            }
        });

        cb.setConverter(new ToStringConverter<>() {
            public String toString(ConnectionProfile profile) {
                return profile != null ? profile.name() : "";
            }
        });

        cb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            defaultCheck.setSelected(Objects.equals(profileManager.getDefaultProfile().orElse(null), newValue));
            autoConnectCheck.setSelected(defaultCheck.isSelected() && profileManager.getAutoConnect());
        });

        return cb;
    }

    private CheckBox initDefaultCheck() {
        var check = newCheckBox(RB, "Default_Profile");
        check.setSelected(Objects.equals(profileManager.getDefaultProfile().orElse(null),
            profileComboBox.getSelectionModel().getSelectedItem()));
        return check;
    }

    private CheckBox initAutoConnectCheck() {
        var check = newCheckBox(RB, "Connect_at_startup");
        check.disableProperty().bind(defaultCheck.selectedProperty().not());
        check.setSelected(defaultCheck.isSelected() && profileManager.getAutoConnect());
        return check;
    }
}
