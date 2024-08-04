/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

import java.util.Collection;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;

public final class MenuUtils {

    public static MenuItem createContextMenuItem(Action action) {
        var menuItem = createMenuItem(action);
        menuItem.acceleratorProperty().unbind();
        menuItem.setAccelerator(null);
        return menuItem;
    }

    public static ContextMenu createContextMenu(Collection<? extends Action> actions) {
        var menu = ActionUtils.createContextMenu(actions);
        menu.getItems().forEach(menuItem -> {
            menuItem.acceleratorProperty().unbind();
            menuItem.setAccelerator(null);
        });
        return menu;
    }

    private MenuUtils() {
    }
}
