/*
 Copyright © 2020-2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

public interface RecordEditorCallback<R> {
    void addRecord(R record);

    void updateRecord(R record);

    void deleteRecord(R record);
}
