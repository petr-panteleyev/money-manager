/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.database;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.money.app.Images;
import java.util.List;
import java.util.function.Consumer;
import static javafx.event.ActionEvent.ACTION;
import static org.panteleyev.fx.ButtonFactory.newButton;
import static org.panteleyev.fx.GridFactory.colSpan;
import static org.panteleyev.fx.GridFactory.newGridPane;
import static org.panteleyev.fx.LabelFactory.newLabel;
import static org.panteleyev.money.app.Constants.COLON;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.Styles.GRID_PANE;

final class TCPEditor extends VBox {
    private final TextField schemaEdit = initSchemaEdit();
    private final TextField dataBaseHostEdit = new TextField();
    private final TextField dataBasePortEdit = new TextField();
    private final TextField dataBaseUserEdit = new TextField();
    private final PasswordField dataBasePasswordEdit = new PasswordField();

    TCPEditor(ValidationSupport validation, Consumer<ActionEvent> createSchemaHanler) {
        var createSchemaButton = newButton(RB, "Create");
        createSchemaButton.setGraphic(new ImageView(Images.WARNING));
        createSchemaButton.disableProperty().bind(validation.invalidProperty());
        createSchemaButton.addEventFilter(ACTION, createSchemaHanler::accept);

        var mySqlGrid = newGridPane(GRID_PANE,
            List.of(newLabel(RB, "Server", COLON), dataBaseHostEdit,
                newLabel(RB, "Port", COLON), dataBasePortEdit),
            List.of(newLabel(RB, "Login", COLON), colSpan(dataBaseUserEdit, 3)),
            List.of(newLabel(RB, "Password", COLON), colSpan(dataBasePasswordEdit, 3)),
            List.of(newLabel(RB, "Schema", COLON), colSpan(schemaEdit, 2), createSchemaButton)
        );

        mySqlGrid.getColumnConstraints().addAll(newColumnConstraints(Priority.NEVER),
            newColumnConstraints(Priority.ALWAYS));

        getChildren().addAll(mySqlGrid);
        VBox.setMargin(mySqlGrid, new Insets(10.0, 10.0, 10.0, 10.0));
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

    void setProfile(ConnectionProfile profile) {
        if (profile != null) {
            setDataBaseHost(profile.dataBaseHost());
            setDataBasePort(profile.dataBasePort());
            setDataBaseUser(profile.dataBaseUser());
            setDataBasePassword(profile.dataBasePassword());
            setSchema(profile.schema());
        } else {
            setDataBaseHost("");
            setDataBasePort(3306);
            setDataBaseUser("");
            setDataBasePassword("");
            setSchema("");
        }
    }
}
