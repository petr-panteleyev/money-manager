package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

public interface RecordEditorCallback<R> {
    void addRecord(R record);
    void updateRecord(R record);
    void deleteRecord(R record);
}
