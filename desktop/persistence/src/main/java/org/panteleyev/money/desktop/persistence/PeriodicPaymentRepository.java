/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Month;

final class PeriodicPaymentRepository extends Repository<PeriodicPayment> {
    public PeriodicPaymentRepository() {
        super("periodic");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO periodic (
                    name, payment_type, recurrence_type, amount, day_of_month, month,
                    account_debited_uuid, account_credited_uuid, contact_uuid,
                    comment, created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE periodic SET
                    name = ?,
                    payment_type = ?,
                    recurrence_type = ?,
                    amount = ?,
                    day_of_month = ?,
                    month = ?,
                    account_debited_uuid = ?,
                    account_credited_uuid = ?,
                    contact_uuid = ?,
                    comment = ?,
                    created = ?,
                    modified = ?
                WHERE uuid = ?
                """;
    }

    @Override
    protected PeriodicPayment fromResultSet(ResultSet rs) throws SQLException {
        return new PeriodicPayment(
                getUuid(rs, "uuid"),
                rs.getString("name"),
                getEnum(rs, "payment_type", PeriodicPaymentType.class),
                getEnum(rs, "recurrence_type", RecurrenceType.class),
                rs.getBigDecimal("amount"),
                rs.getInt("day_of_month"),
                getEnum(rs, "month", Month.class),
                getUuid(rs, "account_debited_uuid"),
                getUuid(rs, "account_credited_uuid"),
                getUuid(rs, "contact_uuid"),
                rs.getString("comment"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, PeriodicPayment payment) throws SQLException {
        var index = 1;
        st.setString(index++, payment.name());
        setEnum(st, index++, payment.paymentType());
        setEnum(st, index++, payment.recurrenceType());
        st.setBigDecimal(index++, payment.amount());
        st.setInt(index++, payment.dayOfMonth());
        setEnum(st, index++, payment.month());
        setUuid(st, index++, payment.accountDebitedUuid());
        setUuid(st, index++, payment.accountCreditedUuid());
        setUuid(st, index++, payment.contactUuid());
        st.setString(index++, payment.comment());
        st.setLong(index++, payment.created());
        st.setLong(index++, payment.modified());
        setUuid(st, index, payment.uuid());
    }
}
