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

package org.panteleyev.money;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;

@SuppressWarnings({"WeakerAccess", "unused"})
public class AccountTreeItem {
    private final ReadOnlyObjectProperty<Account> accountProperty;
    private final ReadOnlyObjectProperty<Category> categoryProperty;
    private final ReadOnlyStringProperty nameProperty;
    private final ReadOnlyStringProperty commentProperty;
    private final int accountId;
    private final int categoryId;

    AccountTreeItem(String name, String comment) {
        accountId = 0;
        categoryId = 0;
        nameProperty = new SimpleStringProperty(this, "name", name);
        commentProperty = new SimpleStringProperty(this, "comment", comment);
        accountProperty = new SimpleObjectProperty<>(this, "account", null);
        categoryProperty = new SimpleObjectProperty<>(this, "category", null);
    }

    AccountTreeItem(Account account) {
        accountId = account.getId();
        categoryId = 0;
        nameProperty = new SimpleStringProperty(this, "name", account.getName());
        commentProperty = new SimpleStringProperty(this, "comment", account.getComment());
        accountProperty = new SimpleObjectProperty<>(this, "account", account);
        categoryProperty = new SimpleObjectProperty<>(this, "category", null);
    }

    AccountTreeItem(Category category) {
        accountId = 0;
        categoryId = category.getId();
        nameProperty = new SimpleStringProperty(this, "name", category.getName());
        commentProperty = new SimpleStringProperty(this, "comment", category.getComment());
        accountProperty = new SimpleObjectProperty<>(this, "account", null);
        categoryProperty = new SimpleObjectProperty<>(this, "category", category);
    }

    int getAccountId() {
        return accountId;
    }

    int getCategoryId() {
        return categoryId;
    }

    public ReadOnlyStringProperty nameProperty() {
        return nameProperty;
    }

    public ReadOnlyStringProperty commentProperty() {
        return commentProperty;
    }

    public ReadOnlyObjectProperty<Account> accountProperty() {
        return accountProperty;
    }

    public ReadOnlyObjectProperty<Category> categoryProperty() {
        return categoryProperty;
    }
}
