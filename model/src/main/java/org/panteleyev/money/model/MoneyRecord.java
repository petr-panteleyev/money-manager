package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.panteleyev.mysqlapi.Record;
import java.util.UUID;

public interface MoneyRecord extends Record<UUID> {
    UUID getUuid();
    long getCreated();
    long getModified();
}
