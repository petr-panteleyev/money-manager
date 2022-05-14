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
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.GRID_PANE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CONNECT_AT_STARTUP;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEFAULT_PROFILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONNECTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_PROFILE;

public class ConnectDialog extends BaseDialog<ConnectionProfile> {
    private final ComboBox<ConnectionProfile> profileComboBox;
    private final CheckBox defaultCheck;
    private final CheckBox autoConnectCheck;

    private final ConnectionProfileManager profileManager;

    public ConnectDialog(ConnectionProfileManager profileManager) {
        super(settings().getDialogCssFileUrl());
        Objects.requireNonNull(profileManager);

        this.profileManager = profileManager;

        profileComboBox = initProfileComboBox();
        defaultCheck = initDefaultCheck();
        autoConnectCheck = initAutoConnectCheck();

        setTitle(fxString(UI, I18N_WORD_CONNECTION));

        var pane = new GridPane();
        pane.getStyleClass().add(GRID_PANE);
        pane.addRow(0, label(fxString(UI, I18N_WORD_PROFILE, COLON)), profileComboBox);
        pane.addRow(1, defaultCheck);
        pane.addRow(2, autoConnectCheck);
        getDialogPane().setContent(pane);

        GridPane.setColumnSpan(defaultCheck, 2);
        GridPane.setColumnSpan(autoConnectCheck, 2);

        profileComboBox.setMaxWidth(Double.MAX_VALUE);

        createDefaultButtons(UI);

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
        var check = newCheckBox(UI, I18N_MISC_DEFAULT_PROFILE);
        check.setSelected(Objects.equals(profileManager.getDefaultProfile().orElse(null),
            profileComboBox.getSelectionModel().getSelectedItem()));
        return check;
    }

    private CheckBox initAutoConnectCheck() {
        var check = newCheckBox(UI, I18N_MISC_CONNECT_AT_STARTUP);
        check.disableProperty().bind(defaultCheck.selectedProperty().not());
        check.setSelected(defaultCheck.isSelected() && profileManager.getAutoConnect());
        return check;
    }
}
