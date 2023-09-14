/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

final class RepositoryUtil {
    static UUID getUuid(ResultSet set, String columnLabel) throws SQLException {
        var obj = set.getObject(columnLabel);
        return (obj instanceof UUID uuid) ? uuid : null;
    }

    static <E extends Enum<E>> E getEnum(ResultSet set, String columnLabel, Class<E> eClass) throws SQLException {
        var obj = set.getObject(columnLabel);
        return obj instanceof String str ? E.valueOf(eClass, str) : null;
    }

    static LocalDate getLocalDate(ResultSet set, String columnLabel) throws SQLException {
        var date = set.getDate(columnLabel);
        return date == null ? null : date.toLocalDate();
    }

    static int convert(boolean value) {
        return value ? 1 : 0;
    }

    static Date convert(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    static String convert(Enum<?> enumValue) {
        return enumValue == null ? null : enumValue.name();
    }

    private RepositoryUtil() {
    }
}
