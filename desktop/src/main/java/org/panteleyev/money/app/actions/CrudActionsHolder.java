/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.actions;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;

import java.util.function.Consumer;

import static org.panteleyev.money.app.actions.ActionBuilder.actionBuilder;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;

public class CrudActionsHolder {
    private final Action createAction;
    private final Action updateAction;
    private final Action deleteAction;
    
    public CrudActionsHolder(
            Consumer<ActionEvent> onCreate,
            Consumer<ActionEvent> onUpdate,
            Consumer<ActionEvent> onDelete,
            BooleanBinding disableBinding
    ) {
        createAction = actionBuilder("Создать...", onCreate)
                .accelerator(SHORTCUT_N)
                .build();
        updateAction = actionBuilder("Изменить...", onUpdate)
                .accelerator(SHORTCUT_E)
                .disableBinding(disableBinding)
                .build();
        deleteAction = actionBuilder("Удалить...", onDelete)
                .accelerator(SHORTCUT_DELETE)
                .disableBinding(disableBinding)
                .build();
    }

    public Action getCreateAction() {
        return createAction;
    }

    public Action getUpdateAction() {
        return updateAction;
    }

    public Action getDeleteAction() {
        return deleteAction;
    }
}
