package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record Operation(String id, String title, BigDecimal amount, String details, LocalDate date) {
    public static Operation of(JsonObject json) {
        var id = json.get("operation_id").getAsString();
        var title = json.get("title").getAsString();

        var detailsElement = json.get("details");
        var details = detailsElement == null ? null : detailsElement.getAsString();

        var direction = json.get("direction").getAsString();
        var absAmount = json.get("amount").getAsBigDecimal();
        var amount = "out".equalsIgnoreCase(direction) ? absAmount.negate() : absAmount;

        var dateString = json.get("datetime").getAsString();
        var local = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        var utc = ZonedDateTime.of(local, ZoneOffset.UTC);
        var date = LocalDate.ofInstant(utc.toInstant(), ZoneId.systemDefault());

        return new Operation(id, title, amount, details, date);
    }
}
