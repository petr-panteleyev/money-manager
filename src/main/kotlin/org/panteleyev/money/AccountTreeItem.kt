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

package org.panteleyev.money

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category


class AccountTreeItem {
    private val accountProperty: ReadOnlyObjectProperty<Account>
    private val categoryProperty: ReadOnlyObjectProperty<Category>
    private val nameProperty: ReadOnlyStringProperty
    private val commentProperty: ReadOnlyStringProperty
    val id: Int

    constructor(name : String, comment : String) {
        id = 0
        nameProperty = SimpleStringProperty(this, "name", name)
        commentProperty = SimpleStringProperty(this, "comment", comment)
        accountProperty = SimpleObjectProperty<Account>(this, "account", null)
        categoryProperty = SimpleObjectProperty<Category>(this, "category", null)
    }

    constructor(account : Account) {
        id = account.id
        nameProperty = SimpleStringProperty(this, "name", account.name)
        commentProperty = SimpleStringProperty(this, "comment", account.comment)
        accountProperty = SimpleObjectProperty(this, "account", account)
        categoryProperty = SimpleObjectProperty<Category>(this, "category", null)
    }

    constructor(category : Category) {
        id = category.id
        nameProperty = SimpleStringProperty(this, "name", category.name)
        commentProperty = SimpleStringProperty(this, "comment", category.comment)
        accountProperty = SimpleObjectProperty<Account>(this, "account", null)
        categoryProperty = SimpleObjectProperty(this, "category", category)
    }

    fun nameProperty(): ReadOnlyStringProperty {
        return nameProperty
    }

    fun commentProperty(): ReadOnlyStringProperty {
        return commentProperty
    }

    fun accountProperty(): ReadOnlyObjectProperty<Account> {
        return accountProperty
    }

    fun categoryProperty(): ReadOnlyObjectProperty<Category> {
        return categoryProperty
    }
}