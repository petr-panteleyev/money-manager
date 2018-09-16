/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.profiles;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.panteleyev.money.Images;
import org.panteleyev.money.Styles;
import org.panteleyev.money.ToStringConverter;
import org.panteleyev.money.ssh.SshManager;
import org.panteleyev.money.ssh.SshSession;
import static org.panteleyev.money.MainWindowController.RB;

final class TCPEditor extends VBox {
    private final TextField schemaEdit = initSchemaEdit();
    private final TextField dataBaseHostEdit = new TextField();
    private final TextField dataBasePortEdit = new TextField();
    private final TextField dataBaseUserEdit = new TextField();
    private final PasswordField dataBasePasswordEdit = new PasswordField();
    private final Button createSchemaButton = new Button(RB.getString("button.Init"));
    private final ChoiceBox<Object> sessionBox = populateSshSessionBox();

    TCPEditor() {
        var mySqlGrid = new GridPane();
        mySqlGrid.getStyleClass().add(Styles.GRID_PANE);

        mySqlGrid.addRow(0, new Label(RB.getString("label.Host")), dataBaseHostEdit,
                new Label(RB.getString("label.Port")), dataBasePortEdit);
        mySqlGrid.addRow(1, new Label(RB.getString("label.User")), dataBaseUserEdit);
        mySqlGrid.addRow(2, new Label(RB.getString("label.Password")), dataBasePasswordEdit);
        mySqlGrid.addRow(3, new Label(RB.getString("label.Schema")), schemaEdit);
        mySqlGrid.addRow(4, new Label(RB.getString("label.Ssh.Tunnel")), sessionBox);
        mySqlGrid.add(createSchemaButton, 3, 3);

        sessionBox.setOnAction(event -> onSessionChange());

        createSchemaButton.setGraphic(new ImageView(Images.WARNING));

        mySqlGrid.getColumnConstraints().addAll(newColumnConstraints(Priority.NEVER),
                newColumnConstraints(Priority.ALWAYS));

        getChildren().addAll(mySqlGrid);
        VBox.setMargin(mySqlGrid, new Insets(10.0, 10.0, 10.0, 10.0));

        GridPane.setColumnSpan(dataBaseUserEdit, 3);
        GridPane.setColumnSpan(dataBasePasswordEdit, 3);
        GridPane.setColumnSpan(schemaEdit, 2);
    }

    Button getCreateSchemaButton() {
        return createSchemaButton;
    }

    TextField getSchemaEdit() {
        return schemaEdit;
    }

    TextField getDataBaseHostEdit() {
        return dataBaseHostEdit;
    }

    TextField getDataBasePortEdit() {
        return dataBasePortEdit;
    }

    TextField getDataBaseUserEdit() {
        return dataBaseUserEdit;
    }

    String getSchema() {
        return schemaEdit.getText();
    }

    void setSchema(String schema) {
        schemaEdit.setText(schema);
    }

    String getDataBaseHost() {
        return dataBaseHostEdit.getText();
    }

    void setDataBaseHost(String host) {
        dataBaseHostEdit.setText(host);
    }

    int getDataBasePort() {
        return Integer.parseInt(dataBasePortEdit.getText());
    }

    void setDataBasePort(int port) {
        dataBasePortEdit.setText(Integer.toString(port));
    }

    String getDataBaseUser() {
        return dataBaseUserEdit.getText();
    }

    void setDataBaseUser(String user) {
        dataBaseUserEdit.setText(user);
    }

    String getDataBasePassword() {
        return dataBasePasswordEdit.getText();
    }

    void setDataBasePassword(String password) {
        dataBasePasswordEdit.setText(password);
    }

    String getSshSession() {
        Object selected = sessionBox.getSelectionModel().getSelectedItem();
        if (selected instanceof SshSession) {
            return ((SshSession) selected).getName();
        } else {
            return "";
        }
    }

    void setSshSession(String name) {
        if (name == null || name.isEmpty()) {
            sessionBox.getSelectionModel().selectFirst();
        } else {
            sessionBox.getSelectionModel()
                    .select(SshManager.getSessions().stream()
                            .filter(s -> s.getName().equals(name)).findFirst().orElseThrow());
        }
    }

    private TextField initSchemaEdit() {
        var schemaEdit = new TextField();
        schemaEdit.setMaxWidth(Double.MAX_VALUE);
        return schemaEdit;
    }

    private static ColumnConstraints newColumnConstraints(Priority hGrow) {
        var constraints = new ColumnConstraints();
        constraints.setHgrow(hGrow);
        return constraints;
    }

    private ChoiceBox<Object> populateSshSessionBox() {
        var comboBox = new ChoiceBox<>();

        comboBox.setConverter(new ToStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof SshSession) {
                    return ((SshSession) object).getName();
                } else {
                    return object.toString();
                }
            }
        });

        comboBox.getItems().add(RB.getString("label.Direct.Connection"));

        var sessions = SshManager.getSessions();
        if (!sessions.isEmpty()) {
            comboBox.getItems().add(new Separator());
            comboBox.getItems().addAll(sessions);
        }
        return comboBox;
    }

    private void onSessionChange() {
        Object selected = sessionBox.getSelectionModel().getSelectedItem();
        if (selected instanceof SshSession) {
            dataBasePortEdit.setText(Integer.toString(((SshSession) selected).getLocalPort()));
        } else {
            dataBasePortEdit.setText("3306");
        }
    }
}
