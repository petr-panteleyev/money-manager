/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.commons.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record TestRecord(
        int intValue,
        Integer integerValue,
        long longValue,
        boolean booleanValue,
        double doubleValue,
        String string,
        UUID uuid,
        BigDecimal bigDecimal,
        LocalDate localDate,
        LocalDateTime localDateTime,
        byte[] bytes
) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestRecord that) {
            return this.intValue == that.intValue
                    && Objects.equals(this.integerValue, that.integerValue)
                    && this.longValue == that.longValue
                    && this.booleanValue == that.booleanValue
                    && this.doubleValue == that.doubleValue
                    && Objects.equals(this.string, that.string)
                    && Objects.equals(this.uuid, that.uuid)
                    && Objects.equals(this.bigDecimal, that.bigDecimal)
                    && Objects.equals(this.localDate, that.localDate)
                    && Objects.equals(this.localDateTime, that.localDateTime)
                    && Arrays.equals(this.bytes, that.bytes);
        } else {
            return false;
        }
    }
}
