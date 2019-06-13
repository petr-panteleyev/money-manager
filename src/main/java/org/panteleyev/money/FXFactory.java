/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public final class FXFactory {
    public static MenuItem newMenuItem(ResourceBundle rb, String key, EventHandler<ActionEvent> action) {
        var menuItem = new MenuItem(rb.getString(key));
        menuItem.setOnAction(action);
        return menuItem;
    }

    public static MenuItem newMenuItem(ResourceBundle rb,
                                       String key,
                                       KeyCombination keyCombination,
                                       EventHandler<ActionEvent> action)
    {
        var menuItem = new MenuItem(rb.getString(key));
        menuItem.setAccelerator(keyCombination);
        menuItem.setOnAction(action);
        return menuItem;
    }

    public static MenuItem newMenuItem(ResourceBundle rb, String key, EventHandler<ActionEvent> action,
                                       BooleanBinding disableBinding)
    {
        var menuItem = newMenuItem(rb, key, action);
        menuItem.disableProperty().bind(disableBinding);
        return menuItem;
    }

    public static MenuBar newMenuBar(Menu... menus) {
        var menuBar = new MenuBar(menus);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    static TextField newSearchField(Consumer<String> valueCallback) {
        var searchField = TextFields.createClearableTextField();
        searchField.setPrefColumnCount(20);
        ((CustomTextField) searchField).setLeft(new ImageView(Images.SEARCH));
        searchField.textProperty().addListener((x, y, newValue) -> valueCallback.accept(newValue));
        searchField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                searchField.clear();
            }
        });
        return searchField;
    }
}
