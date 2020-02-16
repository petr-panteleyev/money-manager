package org.panteleyev.money.ymoney;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;

public class Operation {
    private final String id;
    private final String title;
    private final BigDecimal amount;
    private final String details;
    private final LocalDate date;

    public Operation(JsonObject json) {
        id = json.get("operation_id").getAsString();
        title = json.get("title").getAsString();

        var detailsElement = json.get("details");
        details = detailsElement == null ? null : detailsElement.getAsString();

        var direction = json.get("direction").getAsString();
        var absAmount = json.get("amount").getAsBigDecimal();
        amount = "out".equalsIgnoreCase(direction) ? absAmount.negate() : absAmount;

        var dateString = json.get("datetime").getAsString();
        var local = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        var utc = ZonedDateTime.of(local, ZoneOffset.UTC);
        date = LocalDate.ofInstant(utc.toInstant(), ZoneId.systemDefault());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDetails() {
        return details;
    }

    public LocalDate getDate() {
        return date;
    }
}
