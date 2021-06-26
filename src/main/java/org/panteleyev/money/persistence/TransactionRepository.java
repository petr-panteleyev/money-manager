/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class TransactionRepository extends Repository<Transaction> {

    TransactionRepository(DataSource dataSource) {
        super("transaction", dataSource);
    }

    @Override
    protected String getInsertSql() {
        return """
            INSERT INTO transaction (
                uuid, amount, day, month, year,
                type, comment, checked, acc_debited_uuid, acc_credited_uuid,
                acc_debited_type, acc_credited_type, acc_debited_category_uuid, acc_credited_category_uuid, contact_uuid,
                rate, rate_direction, invoice_number, parent_uuid, detailed,
                statement_date, created, modified
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?
            )
            """;
    }

    @Override
    protected String getUpdateSql() {
        return """
            UPDATE transaction SET
                amount = ?,
                day = ?,
                month = ?,
                year = ?,
                type = ?,
                comment = ?,
                checked = ?,
                acc_debited_uuid = ?,
                acc_credited_uuid = ?,
                acc_debited_type = ?,
                acc_credited_type = ?,
                acc_debited_category_uuid = ?,
                acc_credited_category_uuid = ?,
                contact_uuid = ?,
                rate = ?,
                rate_direction = ?,
                invoice_number = ?,
                parent_uuid = ?,
                detailed = ?,
                statement_date = ?,
                created = ?,
                modified = ?
            WHERE uuid = ?
            """;
    }

    @Override
    protected Transaction fromResultSet(ResultSet rs) throws SQLException {
        return new Transaction(
            getUuid(rs, "uuid"),
            rs.getBigDecimal("amount"),
            rs.getInt("day"),
            rs.getInt("month"),
            rs.getInt("year"),
            getEnum(rs, "type", TransactionType.class),
            rs.getString("comment"),
            getBoolean(rs, "checked"),
            getUuid(rs, "acc_debited_uuid"),
            getUuid(rs, "acc_credited_uuid"),
            getEnum(rs, "acc_debited_type", CategoryType.class),
            getEnum(rs, "acc_credited_type", CategoryType.class),
            getUuid(rs, "acc_debited_category_uuid"),
            getUuid(rs, "acc_credited_category_uuid"),
            getUuid(rs, "contact_uuid"),
            rs.getBigDecimal("rate"),
            rs.getInt("rate_direction"),
            rs.getString("invoice_number"),
            getUuid(rs, "parent_uuid"),
            getBoolean(rs, "detailed"),
            getLocalDate(rs, "statement_date"),
            rs.getLong("created"),
            rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Transaction transaction, boolean update) throws SQLException {
        var index = 1;
        if (!update) {
            setUuid(st, index++, transaction.uuid());
        }
        st.setBigDecimal(index++, transaction.amount());
        st.setInt(index++, transaction.day());
        st.setInt(index++, transaction.month());
        st.setInt(index++, transaction.year());
        setEnum(st, index++, transaction.type());
        st.setString(index++, transaction.comment());
        setBoolean(st, index++, transaction.checked());
        setUuid(st, index++, transaction.accountDebitedUuid());
        setUuid(st, index++, transaction.accountCreditedUuid());
        setEnum(st, index++, transaction.accountDebitedType());
        setEnum(st, index++, transaction.accountCreditedType());
        setUuid(st, index++, transaction.accountDebitedCategoryUuid());
        setUuid(st, index++, transaction.accountCreditedCategoryUuid());
        setUuid(st, index++, transaction.contactUuid());
        st.setBigDecimal(index++, transaction.rate());
        st.setInt(index++, transaction.rateDirection());
        st.setString(index++, transaction.invoiceNumber());
        setUuid(st, index++, transaction.parentUuid());
        setBoolean(st, index++, transaction.detailed());
        setLocalDate(st, index++, transaction.statementDate());
        st.setLong(index++, transaction.created());
        st.setLong(index++, transaction.modified());
        if (update) {
            setUuid(st, index, transaction.uuid());
        }
    }
}
