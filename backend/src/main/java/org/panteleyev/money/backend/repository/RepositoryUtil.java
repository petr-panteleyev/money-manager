/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public interface RepositoryUtil {
    static UUID getUuid(ResultSet set, String columnLabel) throws SQLException {
        var obj = set.getObject(columnLabel);
        return (obj instanceof String str && !str.isBlank()) ? UUID.fromString(str) : null;
    }

    static <E extends Enum<E>> E getEnum(ResultSet set, String columnLabel, Class<E> eClass) throws SQLException {
        var obj = set.getObject(columnLabel);
        return obj instanceof String str ? E.valueOf(eClass, str) : null;
    }

    static LocalDate getLocalDate(ResultSet set, String columnLabel) throws SQLException {
        return set.getObject(columnLabel) == null ? null : LocalDate.ofEpochDay(set.getLong(columnLabel));
    }

    static boolean getBoolean(ResultSet set, String columnLabel) throws SQLException {
        return set.getInt(columnLabel) != 0;
    }

    static int convert(boolean value) {
        return value ? 1 : 0;
    }

    static String convert(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    static Long convert(LocalDate localDate) {
        return localDate == null ? null : localDate.toEpochDay();
    }

    static String convert(Enum<?> enumValue) {
        return enumValue == null ? null : enumValue.name();
    }
}
