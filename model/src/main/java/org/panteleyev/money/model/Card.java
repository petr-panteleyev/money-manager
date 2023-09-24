/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.time.LocalDate;
import java.util.UUID;

public record Card(
        UUID uuid,
        UUID accountUuid,
        CardType type,
        String number,
        LocalDate expiration,
        String comment,
        boolean enabled,
        long created,
        long modified
) implements MoneyRecord {
    public Card {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (accountUuid == null) {
            throw new IllegalStateException("Account UUID cannot be null");
        }

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }

        comment = MoneyRecord.normalize(comment);
    }

    public static final class Builder {
        private UUID uuid = null;
        private UUID accountUuid;
        private CardType type = CardType.MIR;
        private String number = "";
        private LocalDate expiration = LocalDate.now();
        private String comment = "";
        private boolean enabled = true;
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public Builder(Card card) {
            if (card == null) {
                return;
            }

            uuid = card.uuid();
            accountUuid = card.accountUuid();
            type = card.type();
            number = card.number();
            expiration = card.expiration();
            comment = card.comment();
            enabled = card.enabled();
            created = card.created();
            modified = card.modified();
        }

        public Card build() {
            return new Card(uuid, accountUuid, type, number, expiration, comment, enabled,
                    created, modified);
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder accountUuid(UUID accountUuid) {
            this.accountUuid = accountUuid;
            return this;
        }

        public Builder type(CardType type) {
            this.type = type;
            return this;
        }

        public Builder number(String number) {
            this.number = number;
            return this;
        }

        public Builder expiration(LocalDate expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
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
