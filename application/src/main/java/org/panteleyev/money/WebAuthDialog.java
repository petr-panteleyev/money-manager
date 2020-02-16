package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

class WebAuthDialog extends BaseController {
    private final StringProperty responseUri = new SimpleStringProperty();

    WebAuthDialog(String url, String callbackUri) {
        var pane = new BorderPane();

        var view = new WebView();
        var engine = view.getEngine();
        engine.setJavaScriptEnabled(true);

        engine.locationProperty().addListener((x, y, newValue) -> {
            if (newValue.startsWith(callbackUri)) {
                responseUri.set(newValue);
                getStage().close();
            }
        });

        engine.load(url);

        pane.setCenter(view);
        setupWindow(pane);
    }

    ReadOnlyStringProperty responseUriProperty() {
        return responseUri;
    }
}
