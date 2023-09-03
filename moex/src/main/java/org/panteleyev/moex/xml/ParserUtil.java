/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class ParserUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static Integer parseInt(String str) {
        if (str.isBlank()) {
            return null;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static BigDecimal parseNumber(String str) {
        if (str.isBlank()) {
            return null;
        } else {
            try {
                return new BigDecimal(str);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (Exception ex) {
            return null;
        }
    }

    private ParserUtil() {
    }
}
