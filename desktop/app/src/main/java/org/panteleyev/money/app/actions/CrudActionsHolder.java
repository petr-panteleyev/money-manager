// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.actions;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import org.panteleyev.fx.FxAction;

import java.util.function.Consumer;

import static org.panteleyev.fx.FxAction.fxAction;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;

public class CrudActionsHolder {
    private final FxAction createAction = fxAction("Создать...").accelerator(SHORTCUT_N);
    private final FxAction updateAction = fxAction("Изменить...").accelerator(SHORTCUT_E);
    private final FxAction deleteAction = fxAction("Удалить...").accelerator(SHORTCUT_DELETE);

    public CrudActionsHolder(
            Consumer<ActionEvent> onCreate,
            Consumer<ActionEvent> onUpdate,
            Consumer<ActionEvent> onDelete,
            BooleanBinding disableBinding)
    {
        createAction.onAction(onCreate::accept);
        updateAction.onAction(onUpdate::accept)
                .disableProperty().bind(disableBinding);
        deleteAction.onAction(onDelete::accept)
                .disableProperty().bind(disableBinding);
    }

    public FxAction getCreateAction() {
        return createAction;
    }

    public FxAction getUpdateAction() {
        return updateAction;
    }

    public FxAction getDeleteAction() {
        return deleteAction;
    }
}
