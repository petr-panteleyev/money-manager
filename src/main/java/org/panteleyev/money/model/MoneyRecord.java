/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import org.panteleyev.mysqlapi.TableRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public sealed interface MoneyRecord extends TableRecord<UUID>
    permits Account, Category, Contact, Currency, Icon, Transaction {

    UUID uuid();

    long created();

    long modified();

    static BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }
}
