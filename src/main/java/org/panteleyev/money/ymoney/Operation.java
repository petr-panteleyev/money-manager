/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.ymoney;

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class Operation {
    private final String id;
    private final String title;
    private final BigDecimal amount;
    private final String details;
    private final LocalDate date;

    Operation(JsonObject json) {
        id = json.get("operation_id").getAsString();
        title = json.get("title").getAsString();

        var detailsElement = json.get("details");
        details = detailsElement == null ? null : detailsElement.getAsString();

        var direction = json.get("direction").getAsString();
        var absAmount = json.get("amount").getAsBigDecimal();
        amount = "out".equalsIgnoreCase(direction) ? absAmount.negate() : absAmount;

        var dateString = json.get("datetime").getAsString().substring(0, 10);
        date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
    }

    String getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getDetails() {
        return details;
    }

    LocalDate getDate() {
        return date;
    }
}
