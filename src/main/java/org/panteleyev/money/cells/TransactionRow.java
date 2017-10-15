/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.cells;

import javafx.scene.control.TableRow;
import org.panteleyev.money.persistence.SplitTransaction;
import org.panteleyev.money.persistence.Transaction;
import static org.panteleyev.money.Styles.GROUP_CELL;
import static org.panteleyev.money.Styles.GROUP_MEMBER_CELL;
import static org.panteleyev.money.Styles.TRANSACTION_CELL;

public class TransactionRow extends TableRow<Transaction> {
    @Override
    public void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(GROUP_CELL, GROUP_MEMBER_CELL, TRANSACTION_CELL);

        if (item != null && !empty) {
            if (item instanceof SplitTransaction) {
                getStyleClass().add(GROUP_CELL);
            } else {
                if (item.getGroupId() != 0) {
                    getStyleClass().add(GROUP_MEMBER_CELL);
                } else {
                    getStyleClass().add(TRANSACTION_CELL);
                }
            }
        } else {
            getStyleClass().add(TRANSACTION_CELL);
        }
    }
}
