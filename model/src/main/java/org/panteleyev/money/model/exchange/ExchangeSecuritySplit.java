/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.exchange;

import org.panteleyev.money.model.MoneyRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExchangeSecuritySplit(
        UUID uuid,
        UUID securityUuid,
        ExchangeSecuritySplitType type,
        LocalDate date,
        BigDecimal rate,
        String comment,
        long created,
        long modified
) implements MoneyRecord {
    public ExchangeSecuritySplit {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        var now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
        comment = MoneyRecord.normalize(comment);
    }

    public static class Builder {
        private UUID uuid;
        private UUID securityUuid;
        private ExchangeSecuritySplitType type;
        private LocalDate date;
        private BigDecimal rate;
        private String comment;
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public ExchangeSecuritySplit build() {
            return new ExchangeSecuritySplit(
                    uuid,
                    securityUuid,
                    type,
                    date,
                    rate,
                    comment,
                    created,
                    modified
            );
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder securityUuid(UUID securityUuid) {
            this.securityUuid = securityUuid;
            return this;
        }

        public Builder type(ExchangeSecuritySplitType type) {
            this.type = type;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
