/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.actions;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.action.Action;

import java.util.function.Consumer;

public class ActionBuilder {
    private final String text;
    private final Consumer<ActionEvent> handler;
    private KeyCombination accelerator;
    private BooleanBinding disableBinding;

    public static ActionBuilder actionBuilder(String text, Consumer<ActionEvent> handler) {
        return new ActionBuilder(text, handler);
    }

    private ActionBuilder(String text, Consumer<ActionEvent> handler) {
        this.text = text;
        this.handler = handler;
    }

    public ActionBuilder accelerator(KeyCombination accelerator) {
        this.accelerator = accelerator;
        return this;
    }

    public ActionBuilder disableBinding(BooleanBinding disableBinding) {
        this.disableBinding = disableBinding;
        return this;
    }

    public Action build() {
        var action = new Action(text, handler);
        if (accelerator != null) {
            action.setAccelerator(accelerator);
        }
        if (disableBinding != null) {
            action.disabledProperty().bind(disableBinding);
        }
        return action;
    }
}
