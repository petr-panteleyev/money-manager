/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

final class TransactionRepository extends Repository<Transaction> {

    TransactionRepository() {
        super("transaction");
    }

    @Override
    protected String getInsertSql() {
        return """
                INSERT INTO transaction (
                    amount, credit_amount, transaction_date, type,
                    comment, checked, acc_debited_uuid, acc_credited_uuid, acc_debited_type,
                    acc_credited_type, acc_debited_category_uuid, acc_credited_category_uuid, contact_uuid,
                    invoice_number, parent_uuid, detailed,
                    statement_date, created, modified, uuid
                ) VALUES (
                    ?, ?, ?, ?,
                    ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,
                    ?, ?, ?,
                    ?, ?, ?, ?
                )
                """;
    }

    @Override
    protected String getUpdateSql() {
        return """
                UPDATE transaction SET
                    amount = ?,
                    credit_amount = ?,
                    transaction_date = ?,
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
                rs.getBigDecimal("credit_amount"),
                getLocalDate(rs, "transaction_date"),
                getEnum(rs, "type", TransactionType.class),
                rs.getString("comment"),
                rs.getBoolean("checked"),
                getUuid(rs, "acc_debited_uuid"),
                getUuid(rs, "acc_credited_uuid"),
                getEnum(rs, "acc_debited_type", CategoryType.class),
                getEnum(rs, "acc_credited_type", CategoryType.class),
                getUuid(rs, "acc_debited_category_uuid"),
                getUuid(rs, "acc_credited_category_uuid"),
                getUuid(rs, "contact_uuid"),
                rs.getString("invoice_number"),
                getUuid(rs, "parent_uuid"),
                rs.getBoolean("detailed"),
                getLocalDate(rs, "statement_date"),
                rs.getLong("created"),
                rs.getLong("modified")
        );
    }

    @Override
    protected void toStatement(PreparedStatement st, Transaction transaction) throws SQLException {
        var index = 1;
        st.setBigDecimal(index++, transaction.amount());
        st.setBigDecimal(index++, transaction.creditAmount());
        setLocalDate(st, index++, transaction.transactionDate());
        setEnum(st, index++, transaction.type());
        st.setString(index++, transaction.comment());
        st.setBoolean(index++, transaction.checked());
        setUuid(st, index++, transaction.accountDebitedUuid());
        setUuid(st, index++, transaction.accountCreditedUuid());
        setEnum(st, index++, transaction.accountDebitedType());
        setEnum(st, index++, transaction.accountCreditedType());
        setUuid(st, index++, transaction.accountDebitedCategoryUuid());
        setUuid(st, index++, transaction.accountCreditedCategoryUuid());
        setUuid(st, index++, transaction.contactUuid());
        st.setString(index++, transaction.invoiceNumber());
        setUuid(st, index++, transaction.parentUuid());
        st.setBoolean(index++, transaction.detailed());
        setLocalDate(st, index++, transaction.statementDate());
        st.setLong(index++, transaction.created());
        st.setLong(index++, transaction.modified());
        setUuid(st, index, transaction.uuid());
    }
}
