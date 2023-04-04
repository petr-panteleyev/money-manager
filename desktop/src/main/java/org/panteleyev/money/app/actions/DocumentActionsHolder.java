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

public class DocumentActionsHolder {
    private final Action documentsAction;
    private final Action attachDocumentAction;

    public DocumentActionsHolder(
            Consumer<ActionEvent> onDocuments,
            Consumer<ActionEvent> onAttachDocument,
            BooleanBinding disableBinding
    ) {
        this.documentsAction = actionBuilder("Документы...", onDocuments)
                .disableBinding(disableBinding)
                .build();
        this.attachDocumentAction = actionBuilder("Прикрепить документ...", onAttachDocument)
                .disableBinding(disableBinding)
                .build();
    }

    public Action getDocumentsAction() {
        return documentsAction;
    }

    public Action getAttachDocumentAction() {
        return attachDocumentAction;
    }
}
