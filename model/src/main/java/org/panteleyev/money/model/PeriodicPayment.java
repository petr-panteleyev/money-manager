/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.panteleyev.money.model.RecurrenceType.MONTHLY;
import static org.panteleyev.money.model.RecurrenceType.YEARLY;

public record PeriodicPayment(
        UUID uuid,
        String name,
        PeriodicPaymentType paymentType,
        RecurrenceType recurrenceType,
        BigDecimal amount,
        int dayOfMonth,
        Month month,
        UUID accountDebitedUuid,
        UUID accountCreditedUuid,
        UUID contactUuid,
        String comment,
        long created,
        long modified
) implements MoneyRecord, Named {
    public PeriodicPayment {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Name cannot be null or empty");
        }
        if (amount == null) {
            throw new IllegalStateException("Amount cannot be null");
        }
        if (accountDebitedUuid == null) {
            throw new IllegalStateException("Debited account id cannot be null");
        }
        if (accountCreditedUuid == null) {
            throw new IllegalStateException("Credited account id cannot be null");
        }
        if (contactUuid == null) {
            throw new IllegalStateException("Contact id cannot be null");
        }
        if (paymentType == null) {
            paymentType = PeriodicPaymentType.MANUAL_PAYMENT;
        }
        if (recurrenceType == null) {
            recurrenceType = MONTHLY;
        }

        amount = MoneyRecord.normalize(amount, BigDecimal.ZERO);
        comment = MoneyRecord.normalize(comment);

        var now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public LocalDate calculateNextDate() {
        var now = LocalDate.now();

        LocalDate next;
        if (recurrenceType == MONTHLY) {
            next = LocalDate.of(now.getYear(), now.getMonth(), dayOfMonth);
            if (next.isBefore(now)) {
                next = next.plusMonths(1);
            }
        } else if (recurrenceType == YEARLY) {
            next = LocalDate.of(now.getYear(), month, dayOfMonth);
            if (next.isBefore(now)) {
                next = next.plusYears(1);
            }
        } else {
            throw new IllegalStateException("Unsupported recurrence type");
        }
        return next;
    }

    public static final class Builder {
        private UUID uuid;
        private String name;
        private PeriodicPaymentType paymentType;
        private RecurrenceType recurrenceType;
        private int dayOfMonth;
        private Month month = Month.JANUARY;
        private BigDecimal amount = BigDecimal.ZERO;
        private UUID accountDebitedUuid;
        private UUID accountCreditedUuid;
        private UUID contactUuid;
        private String comment;
        private long created = 0L;
        private long modified = 0L;

        public Builder() {
        }

        public Builder(PeriodicPayment payment) {
            if (payment == null) {
                return;
            }

            uuid = payment.uuid();
            name = payment.name();
            paymentType = payment.paymentType();
            recurrenceType = payment.recurrenceType();
            dayOfMonth = payment.dayOfMonth();
            month = payment.month();
            amount = payment.amount();
            accountDebitedUuid = payment.accountDebitedUuid();
            accountCreditedUuid = payment.accountCreditedUuid();
            contactUuid = payment.contactUuid();
            comment = payment.comment();
            created = payment.created();
            modified = payment.modified();
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder paymentType(PeriodicPaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder recurrenceType(RecurrenceType recurrenceType) {
            this.recurrenceType = recurrenceType;
            return this;
        }

        public Builder dayOfMonth(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
            return this;
        }

        public Builder month(Month month) {
            this.month = month;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder accountDebitedUuid(UUID accountDebitedUuid) {
            this.accountDebitedUuid = accountDebitedUuid;
            return this;
        }

        public Builder accountCreditedUuid(UUID accountCreditedUuid) {
            this.accountCreditedUuid = accountCreditedUuid;
            return this;
        }

        public Builder contactUuid(UUID contactUuid) {
            this.contactUuid = contactUuid;
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

        public PeriodicPayment build() {
            return new PeriodicPayment(uuid, name, paymentType, recurrenceType, amount, dayOfMonth, month,
                    accountDebitedUuid, accountCreditedUuid, contactUuid,
                    comment, created, modified
            );
        }
    }
}
